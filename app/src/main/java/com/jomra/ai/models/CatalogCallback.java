package com.jomra.ai.models;

import java.util.List;

public interface CatalogCallback {
    void onSuccess(List<ModelInfo> models);
    void onError(String error);
}
