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

    private static OkHttpClient sharedClient;

    public APIClient() {
        this.client = getSharedClient();
        this.gson = new Gson();
    }

    public static synchronized OkHttpClient getSharedClient() {
        if (sharedClient == null) {
            sharedClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        }
        return sharedClient;
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
