package com.jomra.ai.agents;

import java.util.Map;

public class Action {
    private final String type;
    private final String description;
    private final Map<String, Object> parameters;

    public Action(String type, String description, Map<String, Object> parameters) {
        this.type = type;
        this.description = description;
        this.parameters = parameters;
    }

    public String getType() { return type; }
    public String getDescription() { return description; }
    public Map<String, Object> getParameters() { return parameters; }

    public static Action openApp(String packageName) {
        return new Action("OPEN_APP", "Open application",
            Map.of("package", packageName));
    }

    public static Action sendMessage(String recipient, String message) {
        return new Action("SEND_MESSAGE", "Send message",
            Map.of("recipient", recipient, "message", message));
    }

    public static Action setReminder(String title, long timestampMs) {
        return new Action("SET_REMINDER", "Set reminder",
            Map.of("title", title, "timestamp", timestampMs));
    }

    public static Action useTool(String toolName, Map<String, Object> params) {
        return new Action("USE_TOOL", "Execute tool: " + toolName,
            Map.of("tool", toolName, "params", params));
    }
}
