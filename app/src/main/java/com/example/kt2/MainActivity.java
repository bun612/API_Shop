package com.example.kt2;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ListView listView;
    private List<Product> productList;
    private ProductAdapter adapter;
    private ApiService apiService;
    private ProgressDialog progressDialog;
    private Button btnRefresh; // Nút thêm mới

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ds_sanpham);

        // Initialize views
        listView = findViewById(R.id.lv);
        productList = new ArrayList<>();
        adapter = new ProductAdapter(this, R.layout.item_sp, productList);
        listView.setAdapter(adapter);

        // Initialize API Service
        apiService = ApiClient.getClient().create(ApiService.class);

        // Hiển thị progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang tải dữ liệu sản phẩm...");
        progressDialog.setCancelable(false);

        // Kiểm tra xem server Flask có chạy không
        checkServerConnection();
    }

    private void checkServerConnection() {
        progressDialog.show();
        // Gọi endpoint đơn giản nhất để kiểm tra kết nối
        apiService.getHome().enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Kết nối thành công đến server Flask");
                    // Nếu kết nối thành công, thử khởi tạo dữ liệu
                    initData();
                } else {
                    progressDialog.dismiss();
                    Log.e(TAG, "Server Flask trả về lỗi: " + response.code());
                    showConnectionError("Server trả về lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "Không thể kết nối đến server Flask: " + t.getMessage());
                t.printStackTrace();
                showConnectionError("Không thể kết nối đến server: " + t.getMessage());
            }
        });
    }

    private void initData() {
        Log.d(TAG, "Đang khởi tạo dữ liệu...");
        apiService.initData().enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Khởi tạo dữ liệu thành công");
                    // Sau khi khởi tạo dữ liệu thành công, tải sản phẩm
                    loadProducts();
                } else {
                    Log.e(TAG, "Khởi tạo dữ liệu thất bại: " + response.code());
                    // Vẫn thử tải sản phẩm
                    loadProducts();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Log.e(TAG, "Lỗi khi khởi tạo dữ liệu: " + t.getMessage());
                // Vẫn thử tải sản phẩm
                loadProducts();
            }
        });
    }

    private void loadProducts() {
        Log.d(TAG, "Đang tải danh sách sản phẩm...");
        apiService.getProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    productList.clear();
                    productList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Tải " + response.body().size() + " sản phẩm thành công");

                    // Log chi tiết sản phẩm đầu tiên nếu có
                    if (!productList.isEmpty()) {
                        Product firstProduct = productList.get(0);
                        Log.d(TAG, "Sản phẩm đầu tiên: ID=" + firstProduct.getId() +
                                ", Tên=" + firstProduct.getName() +
                                ", Giá=" + firstProduct.getPrice());
                    } else {
                        Log.w(TAG, "Danh sách sản phẩm trống");
                        Toast.makeText(MainActivity.this,
                                "Không có sản phẩm nào",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorBody = "Unknown error";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.e(TAG, "Lỗi khi tải sản phẩm: " + response.code() + " - " + errorBody);
                    Toast.makeText(MainActivity.this,
                            "Lỗi tải dữ liệu: " + response.code(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "Không thể tải sản phẩm: " + t.getMessage());
                t.printStackTrace();
                Toast.makeText(MainActivity.this,
                        "Lỗi kết nối khi tải sản phẩm: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showConnectionError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        // Có thể thêm nút Retry ở đây
    }
}