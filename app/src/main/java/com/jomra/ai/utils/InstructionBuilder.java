package com.jomra.ai.utils;

import com.jomra.ai.tools.Tool;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InstructionBuilder {
    // BOLT: Cache generated tool instructions to avoid re-calculation
    private static final Map<Integer, String> toolSchemaCache = new ConcurrentHashMap<>();

    /**
     * Builds a comprehensive system instruction including tool schemas.
     */
    public static String buildSystemInstruction(String baseInstruction, Collection<Tool> availableTools) {
        StringBuilder sb = new StringBuilder(baseInstruction);
        sb.append("\n\nYou have access to the following tools:\n");

        sb.append(getToolSchemaString(availableTools));

        sb.append("\nTo use a tool, respond with: TOOL: tool_name { \"param\": \"value\" }");
        return sb.toString();
    }

    private static String getToolSchemaString(Collection<Tool> tools) {
        // BOLT: Use hash of tool names as cache key
        int toolsHash = 0;
        for (Tool t : tools) toolsHash += t.getName().hashCode();

        String cached = toolSchemaCache.get(toolsHash);
        if (cached != null) return cached;

        StringBuilder sb = new StringBuilder();
        for (Tool tool : tools) {
            sb.append("- ").append(tool.getName()).append(": ").append(tool.getDescription()).append("\n");
        }

        String result = sb.toString();
        toolSchemaCache.put(toolsHash, result);
        return result;
    }
}
