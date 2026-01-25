package com.jomra.ai.utils;

import com.jomra.ai.tools.Tool;
import java.util.Collection;

public class InstructionBuilder {
    /**
     * Builds a comprehensive system instruction including tool schemas.
     */
    public static String buildSystemInstruction(String baseInstruction, Collection<Tool> availableTools) {
        StringBuilder sb = new StringBuilder(baseInstruction);
        sb.append("\n\nYou have access to the following tools:\n");

        for (Tool tool : availableTools) {
            sb.append("- ").append(tool.getName()).append(": ").append(tool.getDescription()).append("\n");
            // Add parameters info if needed
        }

        sb.append("\nTo use a tool, respond with: TOOL: tool_name { \"param\": \"value\" }");
        return sb.toString();
    }
}
