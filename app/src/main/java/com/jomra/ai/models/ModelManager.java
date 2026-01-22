package com.jomra.ai.models;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class ModelManager {
    private static final String TAG = "ModelManager";
    private static final String MODELS_DIR = "models";

    private final Context context;
    private final Map<String, Interpreter> loadedModels;
    private final Map<String, GpuDelegate> gpuDelegates;
    private final File modelCacheDir;

    public ModelManager(Context context) {
        this.context = context.getApplicationContext();
        this.loadedModels = new HashMap<>();
        this.gpuDelegates = new HashMap<>();
        this.modelCacheDir = new File(context.getFilesDir(), MODELS_DIR);
        if (!modelCacheDir.exists()) {
            modelCacheDir.mkdirs();
        }
    }

    public Interpreter loadModel(String modelName) {
        if (loadedModels.containsKey(modelName)) {
            Log.i(TAG, "Model already loaded: " + modelName);
            return loadedModels.get(modelName);
        }
        try {
            File cachedModel = new File(modelCacheDir, modelName);
            MappedByteBuffer modelBuffer;
            if (cachedModel.exists() && verifyModel(cachedModel, modelName)) {
                Log.i(TAG, "Loading model from cache: " + modelName);
                modelBuffer = loadModelFile(cachedModel);
            } else {
                Log.i(TAG, "Loading model from assets: " + modelName);
                modelBuffer = loadModelFromAssets(modelName);
            }
            if (modelBuffer == null) {
                Log.e(TAG, "Failed to load model buffer: " + modelName);
                return null;
            }

            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(4); // Increased for performance

            // Try GPU acceleration
            try {
                // RED TEAM: GpuDelegate can throw Throwable (Errors) on some devices
                GpuDelegate delegate = new GpuDelegate();
                options.addDelegate(delegate);
                gpuDelegates.put(modelName, delegate);
                Log.i(TAG, "GPU acceleration enabled for: " + modelName);
            } catch (Throwable t) {
                Log.w(TAG, "GPU acceleration not supported, falling back to CPU: " + t.getMessage());
                try {
                    options.setUseNNAPI(true); // Fallback to NNAPI
                } catch (Throwable ignore) {}
            }

            Interpreter interpreter = new Interpreter(modelBuffer, options);
            loadedModels.put(modelName, interpreter);
            Log.i(TAG, "Model loaded successfully: " + modelName);
            return interpreter;
        } catch (Exception e) {
            Log.e(TAG, "Error loading model: " + modelName, e);
            return null;
        }
    }

    private MappedByteBuffer loadModelFile(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        FileChannel fileChannel = fis.getChannel();
        MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
        fis.close();
        return buffer;
    }

    private MappedByteBuffer loadModelFromAssets(String modelName) throws IOException {
        String assetPath = MODELS_DIR + "/" + modelName;
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(assetPath);
        FileInputStream fis = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fis.getChannel();
        MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY,
            fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());
        fis.close();
        return buffer;
    }

    private boolean verifyModel(File modelFile, String modelName) {
        try {
            String actualHash = computeSHA256(modelFile);
            String expectedHash = getExpectedHash(modelName);
            if (expectedHash == null || expectedHash.equals("dummy")) return true;
            return actualHash.equalsIgnoreCase(expectedHash);
        } catch (Exception e) {
            return false;
        }
    }

    private String computeSHA256(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[8192];
        int read;
        while ((read = fis.read(buffer)) != -1) md.update(buffer, 0, read);
        fis.close();
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private String getExpectedHash(String modelName) {
        try {
            String hashFile = MODELS_DIR + "/" + modelName + ".sha256";
            InputStream is = context.getAssets().open(hashFile);
            byte[] buffer = new byte[64];
            int read = is.read(buffer);
            is.close();
            return new String(buffer, 0, read).trim();
        } catch (Exception e) {
            return null;
        }
    }

    public void unloadAll() {
        for (Interpreter interpreter : loadedModels.values()) interpreter.close();
        loadedModels.clear();
        for (GpuDelegate delegate : gpuDelegates.values()) delegate.close();
        gpuDelegates.clear();
    }

    public int getLoadedModelCount() { return loadedModels.size(); }
}
