package com.jomra.ai.tools;

import android.content.Context;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class BaseTool implements Tool {
    protected final Context context;

    public BaseTool(Context context) {
        this.context = context != null ? context.getApplicationContext() : null;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public ToolParameter[] getParameters() {
        return new ToolParameter[0];
    }

    @Override
    public long getEstimatedDurationMs() {
        return 1000;
    }

    protected ToolResult success(String message) {
        return new ToolResult.Builder()
            .success(true)
            .resultText(message)
            .build();
    }

    protected ToolResult error(String message) {
        return new ToolResult.Builder()
            .success(false)
            .error(message)
            .build();
    }
}
