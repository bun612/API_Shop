package com.example.kt2;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HoaDonActivity extends AppCompatActivity {
    private static final String TAG = "HoaDonActivity";
    private TextView tvMaHD, tvNgayHD, tvTenKH, tvSDT, tvTenSP, tvGiaSP, tvSoLuong, tvThanhTien;
    private ApiService apiService;
    private DatabaseHelper dbHelper;
    private ProgressDialog progressDialog;
    private DecimalFormat decimalFormat = new DecimalFormat("#,###");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hoadon);

        // Khởi tạo các TextView
        tvMaHD = findViewById(R.id.tvMaHD);
        tvNgayHD = findViewById(R.id.tvNgayHD);
        tvTenKH = findViewById(R.id.tvTenKH);
        tvSDT = findViewById(R.id.tvSDT);
        tvTenSP = findViewById(R.id.tvTenSP);
        tvGiaSP = findViewById(R.id.tvGiaSP);
        tvSoLuong = findViewById(R.id.tvSoLuong);
        tvThanhTien = findViewById(R.id.tvThanhTien);

        // Khởi tạo API service
        apiService = ApiClient.getClient().create(ApiService.class);
        dbHelper = new DatabaseHelper(this);

        // Hiển thị progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang tải thông tin hóa đơn...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Lấy order_id từ intent
        long orderId = getIntent().getLongExtra("order_id", -1);
        if (orderId == -1) {
            Toast.makeText(this, "Không tìm thấy mã hóa đơn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set mã hóa đơn
        tvMaHD.setText("Mã HD: " + orderId);

        // Set ngày hôm nay
        tvNgayHD.setText("Ngày: " + dateFormat.format(new Date()));

        // Lấy thông tin hóa đơn từ API
        getOrderDetails(orderId);
    }

    private void getOrderDetails(long orderId) {
        // Gọi API lấy thông tin đơn hàng
        apiService.getOrder(orderId).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                progressDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    // Hiển thị thông tin từ API
                    displayOrderFromApi(response.body());
                    Log.d(TAG, "Lấy thông tin hóa đơn API thành công");
                } else {
                    // Nếu API lỗi, lấy từ database local
                    Log.e(TAG, "Lỗi API: " + response.code());
                    displayOrderFromLocalDb(orderId);
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "Lỗi kết nối API: " + t.getMessage());
                // Nếu kết nối API lỗi, lấy từ database local
                displayOrderFromLocalDb(orderId);
            }
        });
    }

    // Hiển thị thông tin đơn hàng từ API
    private void displayOrderFromApi(Order order) {
        try {
            // Thông tin khách hàng
            tvTenKH.setText("Khách hàng: " + order.getCustomer_name());
            tvSDT.setText("SĐT: " + order.getCustomer_phone());

            // Chỉ lấy sản phẩm đầu tiên (nếu có nhiều)
            if (order.getDetails() != null && !order.getDetails().isEmpty()) {
                Order.OrderDetail detail = order.getDetails().get(0);
                tvTenSP.setText("Sản phẩm: " + detail.getProduct_name());
                tvGiaSP.setText("Giá: " + decimalFormat.format(detail.getPrice()) + " VND");
                tvSoLuong.setText("Số lượng: " + detail.getQuantity());

                // Tính lại tổng tiền dựa trên chi tiết đơn hàng
                double calculatedTotal = 0;
                for (Order.OrderDetail d : order.getDetails()) {
                    calculatedTotal += d.getPrice() * d.getQuantity();
                }

                // Sử dụng tổng tiền đã tính lại nếu order.getTotal() = 0
                double displayTotal = (order.getTotal() > 0) ? order.getTotal() : calculatedTotal;
                tvThanhTien.setText("Thành tiền: " + decimalFormat.format(displayTotal) + " VND");

                Log.d(TAG, "Order total from API: " + order.getTotal());
                Log.d(TAG, "Calculated total: " + calculatedTotal);
            } else {
                Toast.makeText(this, "Không có chi tiết sản phẩm", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi hiển thị thông tin API: " + e.getMessage());
            Toast.makeText(this, "Có lỗi xảy ra khi hiển thị thông tin hóa đơn", Toast.LENGTH_SHORT).show();
            // Thử lấy từ database local
            displayOrderFromLocalDb(order.getId());
        }
    }
    // Hiển thị thông tin đơn hàng từ database local
    private void displayOrderFromLocalDb(long orderId) {
        try {
            // Lấy thông tin đơn hàng từ database
            Order.LocalOrder localOrder = dbHelper.getOrder(orderId);
            if (localOrder != null) {
                // Hiển thị thông tin từ database
                tvTenKH.setText("Khách hàng: " + localOrder.getCustomerName());
                tvSDT.setText("SĐT: " + localOrder.getCustomerPhone());
                tvTenSP.setText("Sản phẩm: " + localOrder.getProductName());
                tvGiaSP.setText("Giá: " + decimalFormat.format(localOrder.getPrice()) + " VND");
                tvSoLuong.setText("Số lượng: " + localOrder.getQuantity());
                tvThanhTien.setText("Thành tiền: " + decimalFormat.format(localOrder.getTotal()) + " VND");

                Log.d(TAG, "Lấy thông tin hóa đơn local thành công");
            } else {
                Toast.makeText(this, "Không tìm thấy thông tin đơn hàng", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Không tìm thấy đơn hàng local với ID: " + orderId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi hiển thị thông tin local: " + e.getMessage());
            Toast.makeText(this, "Có lỗi xảy ra khi hiển thị thông tin hóa đơn", Toast.LENGTH_SHORT).show();
        }
    }
}