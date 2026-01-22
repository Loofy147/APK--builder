package com.jomra.ai.agents;

import java.util.Map;

public class AgentInput {
    private final String text;
    private final InputType type;
    private final Map<String, Object> parameters;
    private final long timestamp;

    public AgentInput(String text, InputType type, Map<String, Object> parameters) {
        this.text = text;
        this.type = type;
        this.parameters = parameters;
        this.timestamp = System.currentTimeMillis();
    }

    public String getText() { return text; }
    public InputType getType() { return type; }
    public Map<String, Object> getParameters() { return parameters; }
    public long getTimestamp() { return timestamp; }

    public enum InputType {
        TEXT, VOICE, IMAGE, FEEDBACK, SYSTEM
    }
}
