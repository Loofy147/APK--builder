package com.jomra.ai.api;

import android.util.Log;
import okhttp3.*;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class APIClient {
    private static final String TAG = "APIClient";
    private final OkHttpClient client;
    private final Gson gson;

    public APIClient() {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();
        this.gson = new Gson();
    }

    public void get(String url, Callback callback) {
        Request request = new Request.Builder()
            .url(url)
            .build();
        client.newCall(request).enqueue(callback);
    }

    public <T> T getSync(String url, Class<T> responseType) throws IOException {
        Request request = new Request.Builder()
            .url(url)
            .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return gson.fromJson(response.body().string(), responseType);
        }
    }
}
