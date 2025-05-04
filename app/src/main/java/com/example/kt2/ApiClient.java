package com.example.kt2;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static Retrofit retrofit = null;
    // Hãy đảm bảo địa chỉ IP này là chính xác
    // Nếu bạn chạy trên máy thật, cần dùng địa chỉ IP của máy tính
    // Nếu bạn dùng emulator, có thể thử 10.0.2.2 để kết nối đến localhost của máy host
    private static final String BASE_URL = "http://192.168.1.62:1234/"; // Thay đổi IP nếu cần

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Add logging
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(60, TimeUnit.SECONDS) // Tăng timeout
                    .readTimeout(60, TimeUnit.SECONDS)    // Tăng timeout
                    .writeTimeout(60, TimeUnit.SECONDS)   // Tăng timeout
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}