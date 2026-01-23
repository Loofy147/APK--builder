package com.jomra.ai.models;

import java.io.File;

public interface DownloadListener {
    void onStarted(ModelInfo model);
    void onProgress(ModelInfo model, long downloaded, long total, float progress);
    void onCompleted(ModelInfo model, File file);
    void onError(String error);
}
