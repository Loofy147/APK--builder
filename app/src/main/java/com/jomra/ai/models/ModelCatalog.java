package com.jomra.ai.models;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.jomra.ai.storage.SecureStorage;
import java.io.*;
import java.util.*;
import okhttp3.*;

public class ModelCatalog {
    private static final String TAG = "ModelCatalog";
    private static final String CATALOG_URL = "https://api.jomra.ai/v2/models/catalog.json";

    private final Context context;
    private final SecureStorage secureStorage;
    private final Gson gson;
    private final Map<String, ModelInfo> catalog = new HashMap<>();

    public ModelCatalog(Context context) {
        this.context = context.getApplicationContext();
        this.secureStorage = new SecureStorage(context);
        this.gson = new Gson();
        loadBundledCatalog();
    }

    public void refreshCatalog(CatalogCallback callback) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(CATALOG_URL).build();
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    CatalogResponse catalogResponse = gson.fromJson(json, CatalogResponse.class);
                    if (catalogResponse != null && catalogResponse.models != null) {
                        for (ModelInfo model : catalogResponse.models) {
                            catalog.put(model.id, model);
                        }
                        callback.onSuccess(new ArrayList<>(catalog.values()));
                    }
                } else {
                    callback.onError("Failed to fetch catalog");
                }
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    private void loadBundledCatalog() {
        try {
            InputStream is = context.getAssets().open("models/catalog.json");
            Reader reader = new InputStreamReader(is);
            CatalogResponse response = gson.fromJson(reader, CatalogResponse.class);
            if (response != null && response.models != null) {
                for (ModelInfo model : response.models) {
                    catalog.put(model.id, model);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to load bundled catalog", e);
        }
    }

    public ModelInfo getModelInfo(String modelId) {
        return catalog.get(modelId);
    }

    public List<ModelInfo> getModelsByCategory(ModelCategory category) {
        List<ModelInfo> results = new ArrayList<>();
        for (ModelInfo model : catalog.values()) {
            if (model.category == category) results.add(model);
        }
        return results;
    }

    public List<ModelInfo> searchModels(String query) {
        List<ModelInfo> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (ModelInfo model : catalog.values()) {
            if (model.name.toLowerCase().contains(lowerQuery)) results.add(model);
        }
        return results;
    }

    public List<ModelInfo> getDownloadedModels() {
        List<ModelInfo> results = new ArrayList<>();
        for (ModelInfo model : catalog.values()) {
            if (isModelDownloaded(model.id)) results.add(model);
        }
        return results;
    }

    public List<ModelInfo> getRecommendedModels() {
        return new ArrayList<>(catalog.values()).subList(0, Math.min(3, catalog.size()));
    }

    public boolean isModelDownloaded(String modelId) {
        String path = secureStorage.getString("model_path_" + modelId, null);
        return path != null && new File(path).exists();
    }

    public void markDownloaded(String modelId, String path) {
        secureStorage.putString("model_path_" + modelId, path);
    }
}

class CatalogResponse {
    public List<ModelInfo> models;
}

interface CatalogCallback {
    void onSuccess(List<ModelInfo> models);
    void onError(String error);
}
