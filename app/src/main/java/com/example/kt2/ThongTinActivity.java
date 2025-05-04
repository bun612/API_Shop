package com.example.kt2;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThongTinActivity extends AppCompatActivity {
    private static final String TAG = "ThongTinActivity";
    private EditText edtName, edtSoDienThoai;
    private Button btnSend;
    private ApiService apiService;
    private DatabaseHelper dbHelper;
    private long productId;
    private double productPrice;
    private String productName;
    private Product product;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thongtin);

        // Khởi tạo
        edtName = findViewById(R.id.edtName);
        edtSoDienThoai = findViewById(R.id.edtSĐT);
        btnSend = findViewById(R.id.btnSend);
        apiService = ApiClient.getClient().create(ApiService.class);
        dbHelper = new DatabaseHelper(this);
        progressDialog = new ProgressDialog(this);

        // Lấy thông tin sản phẩm từ intent
        productId = getIntent().getLongExtra("product_id", -1);
        productPrice = getIntent().getDoubleExtra("product_price", 0);
        productName = getIntent().getStringExtra("product_name");

        btnSend.setOnClickListener(v -> {
            String name = edtName.getText().toString();
            String phone = edtSoDienThoai.getText().toString();

            if (!name.isEmpty() && !phone.isEmpty()) {
                progressDialog.setMessage("Đang xử lý đơn hàng...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                // Tạo khách hàng mới thông qua API
                createCustomerAndOrder(name, phone);
            } else {
                Toast.makeText(ThongTinActivity.this,
                        "Vui lòng nhập đầy đủ thông tin",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Phương thức mới - Tạo khách hàng trước, sau đó tạo đơn hàng
    private void createCustomerAndOrder(String name, String phone) {
        // Tạo đối tượng Customer
        Customer customer = new Customer();
        customer.setName(name);
        customer.setPhone(phone);

        // Gọi API tạo khách hàng
        apiService.createCustomer(customer).enqueue(new Callback<Customer>() {
            @Override
            public void onResponse(Call<Customer> call, Response<Customer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Lấy ID khách hàng từ response
                    long customerId = response.body().getId();
                    Log.d(TAG, "Tạo khách hàng thành công, ID: " + customerId);

                    // Lưu khách hàng vào DB local
                    dbHelper.addCustomer(name, phone);

                    // Tiếp tục tạo đơn hàng với ID khách hàng mới
                    createOrder(customerId);
                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(Call<Customer> call, Throwable t) {
                handleNetworkError(t);
            }
        });
    }

    // Phương thức tạo đơn hàng với customerId đã biết
    private void createOrder(long customerId) {
        // Tạo đơn hàng
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCustomer_id(customerId);

        // Tạo danh sách sản phẩm trong đơn hàng
        List<OrderRequest.OrderProduct> products = new ArrayList<>();
        OrderRequest.OrderProduct orderProduct = new OrderRequest.OrderProduct();
        orderProduct.setProduct_id(productId);
        orderProduct.setQuantity(1);
        orderProduct.setPrice(productPrice);
        products.add(orderProduct);

        // Tính tổng tiền dựa trên giá sản phẩm và số lượng
        double total = productPrice * 1; // Số lượng * giá

        // Thêm danh sách sản phẩm và tổng tiền vào request
        orderRequest.setProducts(products);
        orderRequest.setTotal(total);

        // Gọi API tạo đơn hàng
        apiService.createOrder(orderRequest).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    // Lấy ID đơn hàng từ response
                    long orderId = response.body().getOrder_id();
                    Log.d(TAG, "Tạo đơn hàng thành công, ID: " + orderId);

                    // Lưu đơn hàng vào DB local
                    long localOrderId = dbHelper.createOrder(customerId, productPrice);
                    dbHelper.addOrderDetail(localOrderId, productId, 1, productPrice);

                    // Chuyển đến màn hình hóa đơn
                    Intent intent = new Intent(ThongTinActivity.this, HoaDonActivity.class);
                    intent.putExtra("order_id", orderId);
                    startActivity(intent);
                    finish();
                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                progressDialog.dismiss();
                handleNetworkError(t);
            }
        });
    }

    // Xử lý lỗi API
    private void handleApiError(Response<?> response) {
        progressDialog.dismiss();
        String errorMessage = "Lỗi kết nối API: " + response.code();
        try {
            if (response.errorBody() != null) {
                errorMessage = response.errorBody().string();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, errorMessage);

        // Sử dụng DB local trong trường hợp có lỗi API
        fallbackToLocalDatabase();
    }

    // Xử lý lỗi mạng
    private void handleNetworkError(Throwable t) {
        progressDialog.dismiss();
        Log.e(TAG, "Lỗi kết nối: " + t.getMessage());

        // Sử dụng DB local trong trường hợp có lỗi mạng
        fallbackToLocalDatabase();
    }

    // Phương thức sử dụng DB local khi có lỗi
    private void fallbackToLocalDatabase() {
        String name = edtName.getText().toString();
        String phone = edtSoDienThoai.getText().toString();

        // Lưu thông tin khách hàng vào DB local
        long customerId = dbHelper.addCustomer(name, phone);

        // Lưu đơn hàng vào DB local
        long orderId = dbHelper.createOrder(customerId, productPrice);
        dbHelper.addOrderDetail(orderId, productId, 1, productPrice);

        Toast.makeText(ThongTinActivity.this,
                "Có lỗi xảy ra với API. Đã lưu đơn hàng vào cơ sở dữ liệu local.",
                Toast.LENGTH_LONG).show();

        // Chuyển đến màn hình hóa đơn với dữ liệu local
        Intent intent = new Intent(ThongTinActivity.this, HoaDonActivity.class);
        intent.putExtra("order_id", orderId);
        startActivity(intent);
        finish();
    }
}