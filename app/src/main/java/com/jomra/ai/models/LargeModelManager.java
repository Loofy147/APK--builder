package com.jomra.ai.models;

import android.content.Context;
import android.util.Log;
import com.jomra.ai.api.APIClient;
import com.jomra.ai.storage.SecureStorage;
import okhttp3.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class LargeModelManager {
    private static final String TAG = "LargeModelManager";
    private final Context context;
    private final SecureStorage secureStorage;
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    private final ModelCatalog catalog;

    public LargeModelManager(Context context) {
        this.context = context.getApplicationContext();
        this.secureStorage = new SecureStorage(context);
        this.catalog = ModelCatalog.getInstance(context);
    }

    public void downloadModel(String modelId, DownloadListener listener) {
        ModelInfo model = catalog.getModelInfo(modelId);
        if (model == null) {
            listener.onError("Model not found");
            return;
        }

        executor.submit(() -> {
            try {
                listener.onStarted(model);
                File modelDir = new File(context.getFilesDir(), "models");
                if (!modelDir.exists()) modelDir.mkdirs();
                File tempFile = new File(modelDir, model.id + ".tmp");
                File finalFile = new File(modelDir, model.filename);

                long downloaded = tempFile.exists() ? tempFile.length() : 0;
                OkHttpClient client = APIClient.getSharedClient();
                Request request = new Request.Builder()
                        .url(model.downloadUrl)
                        .addHeader("Range", "bytes=" + downloaded + "-")
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful() && response.code() != 206) {
                    throw new IOException("Unexpected code " + response);
                }

                InputStream is = response.body().byteStream();
                RandomAccessFile raf = new RandomAccessFile(tempFile, "rw");
                raf.seek(downloaded);

                byte[] buffer = new byte[8192];
                int read;
                long total = model.sizeBytes;
                long lastUpdate = 0;
                while ((read = is.read(buffer)) != -1) {
                    raf.write(buffer, 0, read);
                    downloaded += read;

                    long now = System.currentTimeMillis();
                    if (now - lastUpdate > 500) { // Throttle updates to 500ms
                        listener.onProgress(model, downloaded, total, (float) downloaded / total);
                        lastUpdate = now;
                    }
                }
                raf.close();
                is.close();

                // SHA-256 Verification
                if (model.sha256 != null) {
                    String actualHash = computeSHA256(tempFile);
                    if (!actualHash.equalsIgnoreCase(model.sha256)) {
                        throw new IOException("SHA-256 verification failed");
                    }
                }

                if (tempFile.renameTo(finalFile)) {
                    catalog.markDownloaded(model.id, finalFile.getAbsolutePath());
                    listener.onCompleted(model, finalFile);
                } else {
                    throw new IOException("Failed to rename file");
                }
            } catch (Exception e) {
                Log.e(TAG, "Download failed", e);
                listener.onError(e.getMessage());
            }
        });
    }

    public boolean isWifiOnly() {
        return secureStorage.getString("download_wifi_only", "true").equals("true");
    }

    public boolean isChargingOnly() {
        return secureStorage.getString("download_charging_only", "false").equals("true");
    }

    private String computeSHA256(File file) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) md.update(buffer, 0, read);
        }
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
