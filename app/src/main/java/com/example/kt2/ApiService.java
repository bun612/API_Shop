package com.example.kt2;

import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;

public interface ApiService {
    // Products
    @GET("products")
    Call<List<Product>> getProducts();

    @GET("products/{id}")
    Call<Product> getProduct(@Path("id") long id);

    @POST("products")
    Call<Product> createProduct(@Body Product product);

    @PUT("products/{id}")
    Call<Void> updateProduct(@Path("id") long id, @Body Product product);

    @DELETE("products/{id}")
    Call<Void> deleteProduct(@Path("id") long id);

    // Customers
    @GET("customers")
    Call<List<Customer>> getCustomers();

    @GET("customers/{id}")
    Call<Customer> getCustomer(@Path("id") long id);

    @POST("customers")
    Call<Customer> createCustomer(@Body Customer customer);

    @PUT("customers/{id}")
    Call<Void> updateCustomer(@Path("id") long id, @Body Customer customer);

    @DELETE("customers/{id}")
    Call<Void> deleteCustomer(@Path("id") long id);

    // Orders
    @GET("orders")
    Call<List<Order>> getOrders();

    @POST("orders")
    Call<OrderResponse> createOrder(@Body OrderRequest orderRequest);

    @GET("orders/{id}")
    Call<Order> getOrder(@Path("id") long id);

    @PUT("orders/{id}")
    Call<Void> updateOrder(@Path("id") long id, @Body OrderRequest orderRequest);

    @DELETE("orders/{id}")
    Call<Void> deleteOrder(@Path("id") long id);

    // Khởi tạo dữ liệu
    @GET("init-data")
    Call<Object> initData();

    // Kiểm tra kết nối
    @GET("/")
    Call<Object> getHome();
}