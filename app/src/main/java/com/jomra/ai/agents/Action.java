package com.jomra.ai.agents;

import java.util.Collections;
import java.util.HashMap;
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
            Collections.singletonMap("package", (Object) packageName));
    }

    public static Action sendMessage(String recipient, String message) {
        Map<String, Object> params = new HashMap<>();
        params.put("recipient", recipient);
        params.put("message", message);
        return new Action("SEND_MESSAGE", "Send message", params);
    }

    public static Action setReminder(String title, long timestampMs) {
        Map<String, Object> params = new HashMap<>();
        params.put("title", title);
        params.put("timestamp", timestampMs);
        return new Action("SET_REMINDER", "Set reminder", params);
    }

    public static Action useTool(String toolName, Map<String, Object> params) {
        Map<String, Object> allParams = new HashMap<>();
        allParams.put("tool", toolName);
        allParams.put("params", params);
        return new Action("USE_TOOL", "Execute tool: " + toolName, allParams);
    }
}
