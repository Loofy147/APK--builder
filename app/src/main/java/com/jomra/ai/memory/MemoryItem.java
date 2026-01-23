package com.jomra.ai.memory;

import java.util.HashMap;
import java.util.Map;

public class MemoryItem {
    public String id;
    public String userInput;
    public String agentResponse;
    public long timestamp;
    public float importance;
    public Map<String, Object> metadata;
    public float[] embedding;
    public float relevanceScore;

    public MemoryItem(String id, String userInput, String agentResponse,
              long timestamp, float importance, Map<String, Object> metadata) {
        this.id = id;
        this.userInput = userInput;
        this.agentResponse = agentResponse;
        this.timestamp = timestamp;
        this.importance = importance;
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }
}
