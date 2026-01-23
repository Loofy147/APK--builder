package com.jomra.ai.api;

import android.util.Log;
import okhttp3.*;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class APIClient {
    private static final String TAG = "APIClient";
    private static OkHttpClient sharedClient;
    private final Gson gson;

    public APIClient() {
        this.gson = new Gson();
    }

    public static synchronized OkHttpClient getSharedClient() {
        if (sharedClient == null) {
            sharedClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    Response response = null;
                    int tryCount = 0;
                    while (response == null && tryCount < 3) {
                        try {
                            response = chain.proceed(request);
                        } catch (Exception e) {
                            tryCount++;
                            if (tryCount >= 3) throw e;
                            Log.w(TAG, "Retrying request... (" + tryCount + ")");
                        }
                    }
                    return response;
                })
                .build();
        }
        return sharedClient;
    }

    public void get(String url, Callback callback) {
        Request request = new Request.Builder()
            .url(url)
            .build();
        getSharedClient().newCall(request).enqueue(callback);
    }

    public <T> T getSync(String url, Class<T> responseType) throws IOException {
        Request request = new Request.Builder()
            .url(url)
            .build();
        try (Response response = getSharedClient().newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return gson.fromJson(response.body().string(), responseType);
        }
    }
}
