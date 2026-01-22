package com.jomra.ai.agents;

import android.content.Context;
import android.util.Log;
import com.jomra.ai.tools.*;
import java.util.*;

public class ToolAgent implements Agent {
    private static final String TAG = "ToolAgent";
    private final Context context;
    private final ToolRegistry toolRegistry;
    private boolean initialized = false;

    public ToolAgent(Context context, ToolRegistry toolRegistry) {
        this.context = context.getApplicationContext();
        this.toolRegistry = toolRegistry;
    }

    @Override public String getId() { return "tool_agent"; }
    @Override public String getName() { return "Tool Agent"; }
    @Override public AgentCapability[] getCapabilities() {
        return new AgentCapability[]{AgentCapability.TOOL_USAGE};
    }

    @Override public boolean initialize() { initialized = true; return true; }

    @Override public AgentResponse process(AgentContext context, AgentInput input) throws AgentException {
        if (!initialized) throw new AgentException(AgentException.ErrorType.CONFIGURATION_ERROR, "Not initialized");

        String query = input.getText();
        if (query == null || query.trim().isEmpty()) {
            return AgentResponse.error("Empty input");
        }

        String lowerQuery = query.toLowerCase();
        String toolName = null;
        Map<String, Object> params = new HashMap<>();

        if (lowerQuery.contains("calculate") || lowerQuery.contains("math")) {
            toolName = "calculator";
            String expr = query.replaceAll("(?i)calculate|math", "").trim();
            params.put("expression", expr);
        } else if (lowerQuery.contains("system") || lowerQuery.contains("device")) {
            toolName = "system_info";
        } else if (lowerQuery.contains("search") || lowerQuery.contains("find")) {
            toolName = "web_search";
            String searchQuery = query.replaceAll("(?i)search|find", "").trim();
            params.put("query", searchQuery);
        } else if (lowerQuery.contains("notify") || lowerQuery.contains("remind")) {
            toolName = "notification";
            params.put("title", "AI Assistant");
            String msg = query.replaceAll("(?i)notify|remind|me", "").trim();
            params.put("message", msg);
        } else if (lowerQuery.contains("list apps") || lowerQuery.contains("show apps")) {
            toolName = "list_apps";
        }

        if (toolName == null) {
            return new AgentResponse.Builder()
                .status(AgentResponse.ResponseStatus.INSUFFICIENT_CONFIDENCE)
                .text("I'm not sure which tool to use for this request.")
                .build();
        }

        Tool tool = toolRegistry.getTool(toolName);
        if (tool == null) return AgentResponse.error("Tool not found: " + toolName);

        try {
            ToolResult result = tool.execute(params);

            return new AgentResponse.Builder()
                .status(result.isSuccess() ? AgentResponse.ResponseStatus.SUCCESS : AgentResponse.ResponseStatus.ERROR)
                .text(result.getResultText())
                .confidence(1.0f)
                .metadata(result.getData())
                .build();
        } catch (Exception e) {
            Log.e(TAG, "Tool execution failed", e);
            return AgentResponse.error("Tool execution failed: " + e.getMessage());
        }
    }

    @Override public void shutdown() { initialized = false; }
    @Override public HealthStatus getHealthStatus() {
        return initialized ? HealthStatus.healthy() : HealthStatus.unhealthy("Not initialized");
    }
    @Override public long getEstimatedLatencyMs() { return 500; }
}
