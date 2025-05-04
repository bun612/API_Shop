package com.example.kt2;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.text.DecimalFormat;
import java.util.List;

public class ProductAdapter extends ArrayAdapter<Product> {
    private Context context;
    private int resource;
    private List<Product> productList;
    private DecimalFormat decimalFormat = new DecimalFormat("#,###");

    public ProductAdapter(@NonNull Context context, int resource, @NonNull List<Product> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.productList = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = LayoutInflater.from(context);
            v = vi.inflate(resource, null);
        }

        Product product = productList.get(position);

        if (product != null) {
            TextView tvName = v.findViewById(R.id.tvName);
            TextView tvPrice = v.findViewById(R.id.tvPrice);
            ImageView imgProduct = v.findViewById(R.id.imgProduct);
            Button btnDetail = v.findViewById(R.id.btnDetail);
            Button btnBuy = v.findViewById(R.id.btnBuy);

            // Set text
            tvName.setText(product.getName());
            tvPrice.setText(decimalFormat.format(product.getPrice()) + " VND");

            // Load image with Glide
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                // Tạo request options
                RequestOptions requestOptions = new RequestOptions()
                        .placeholder(R.drawable.placeholder_image) // Thay thế bằng drawable của bạn
                        .error(R.drawable.error_image) // Thay thế bằng drawable của bạn
                        .diskCacheStrategy(DiskCacheStrategy.ALL); // Cache hình ảnh

                // Tải và hiển thị hình ảnh
                Glide.with(context)
                        .load(product.getImage())
                        .apply(requestOptions)
                        .into(imgProduct);
            } else {
                // Nếu không có URL hình ảnh, hiển thị hình placeholder
                imgProduct.setImageResource(R.drawable.placeholder_image); // Thay thế bằng drawable của bạn
            }

            // Set click listeners
            btnDetail.setOnClickListener(view -> {
                Intent intent = new Intent(context, ChiTietActivity.class);
                intent.putExtra("product_id", product.getId());
                context.startActivity(intent);
            });

            btnBuy.setOnClickListener(view -> {
                Intent intent = new Intent(context, ThongTinActivity.class);
                intent.putExtra("product_id", product.getId());
                intent.putExtra("product_name", product.getName());
                intent.putExtra("product_price", product.getPrice());
                context.startActivity(intent);
            });
        }

        return v;
    }
}