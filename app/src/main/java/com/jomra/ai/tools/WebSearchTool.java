package com.jomra.ai.tools;

import android.content.Context;
import java.util.Map;

public class WebSearchTool implements Tool {
    private final Context context;
    public WebSearchTool(Context context) { this.context = context.getApplicationContext(); }
    @Override public String getName() { return "web_search"; }
    @Override public String getDescription() { return "Search web"; }
    @Override public ToolParameter[] getParameters() {
        return new ToolParameter[]{new ToolParameter("query", "Search query", String.class, true, "")};
    }
    @Override public ToolResult execute(Map<String, Object> params) throws ToolException {
        long start = System.currentTimeMillis();
        String query = (String) params.get("query");
        return new ToolResult.Builder().success(true).resultText("Results for " + query)
            .executionTime(System.currentTimeMillis() - start).build();
    }
    @Override public long getEstimatedDurationMs() { return 1000; }
    @Override public boolean isAvailable() { return true; }
}
