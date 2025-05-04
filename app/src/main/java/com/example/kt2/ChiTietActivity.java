package com.example.kt2;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.text.DecimalFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChiTietActivity extends AppCompatActivity {
    private static final String TAG = "ChiTietActivity";
    private ImageView imgProduct;
    private TextView tvName, tvPrice, tvDescription;
    private Button btnBuy;
    private ApiService apiService;
    private ProgressDialog progressDialog;
    private long productId;
    private DecimalFormat decimalFormat = new DecimalFormat("#,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chitiet);

        // Khởi tạo views
        imgProduct = findViewById(R.id.imgProduct);
        tvName = findViewById(R.id.tvName);
        tvPrice = findViewById(R.id.tvPrice);
        tvDescription = findViewById(R.id.tvDescription);
        btnBuy = findViewById(R.id.btnSend);

        // Khởi tạo API client
        apiService = ApiClient.getClient().create(ApiService.class);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang tải thông tin sản phẩm...");
        progressDialog.setCancelable(false);

        // Lấy product_id từ intent
        productId = getIntent().getLongExtra("product_id", -1);
        if (productId == -1) {
            Toast.makeText(this, "Không thể tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Tải thông tin sản phẩm
        loadProductDetails();

        // Xử lý sự kiện nút mua
        btnBuy.setOnClickListener(v -> {
            Intent intent = new Intent(ChiTietActivity.this, ThongTinActivity.class);
            intent.putExtra("product_id", productId);
            intent.putExtra("product_name", tvName.getText().toString());
            intent.putExtra("product_price", getIntent().getDoubleExtra("product_price", 0));
            startActivity(intent);
        });
    }

    private void loadProductDetails() {
        progressDialog.show();

        apiService.getProduct(productId).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                progressDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    Product product = response.body();

                    // Hiển thị thông tin
                    tvName.setText(product.getName());
                    tvPrice.setText(decimalFormat.format(product.getPrice()) + " VND");
                    tvDescription.setText(product.getDescription());

                    // Tải hình ảnh với Glide
                    if (product.getImage() != null && !product.getImage().isEmpty()) {
                        RequestOptions requestOptions = new RequestOptions()
                                .placeholder(R.drawable.placeholder_image)
                                .error(R.drawable.error_image)
                                .diskCacheStrategy(DiskCacheStrategy.ALL);

                        Glide.with(ChiTietActivity.this)
                                .load(product.getImage())
                                .apply(requestOptions)
                                .into(imgProduct);
                    }
                } else {
                    Toast.makeText(ChiTietActivity.this,
                            "Không thể tải thông tin sản phẩm",
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading product: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(ChiTietActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Network error: " + t.getMessage());
            }
        });
    }
}