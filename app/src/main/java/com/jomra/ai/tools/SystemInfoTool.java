package com.jomra.ai.tools;

import android.content.Context;
import android.os.Build;
import java.util.*;

public class SystemInfoTool implements Tool {
    private final Context context;
    public SystemInfoTool(Context context) { this.context = context.getApplicationContext(); }
    @Override public String getName() { return "system_info"; }
    @Override public String getDescription() { return "Get device info"; }
    @Override public ToolParameter[] getParameters() { return new ToolParameter[0]; }
    @Override public ToolResult execute(Map<String, Object> params) {
        long start = System.currentTimeMillis();
        Map<String, Object> info = new HashMap<>();
        info.put("model", Build.MODEL);
        info.put("version", Build.VERSION.RELEASE);
        return new ToolResult.Builder().success(true).resultText("Info: " + info.toString())
            .data(info).executionTime(System.currentTimeMillis() - start).build();
    }
    @Override public long getEstimatedDurationMs() { return 50; }
    @Override public boolean isAvailable() { return true; }
}
