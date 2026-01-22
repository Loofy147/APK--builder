package com.jomra.ai.tools;

import java.util.Map;

public interface Tool {
    String getName();
    String getDescription();
    ToolParameter[] getParameters();
    ToolResult execute(Map<String, Object> params) throws ToolException;
    long getEstimatedDurationMs();
    boolean isAvailable();
}
