package com.jomra.ai.tools;

import java.util.HashMap;
import java.util.Map;

public class ToolResult {
    private final boolean success;
    private final String resultText;
    private final Map<String, Object> data;
    private final long executionTimeMs;
    private final String errorMessage;

    private ToolResult(Builder builder) {
        this.success = builder.success;
        this.resultText = builder.resultText;
        this.data = builder.data;
        this.executionTimeMs = builder.executionTimeMs;
        this.errorMessage = builder.errorMessage;
    }

    public boolean isSuccess() { return success; }
    public String getResultText() { return resultText; }
    public Map<String, Object> getData() { return data; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    public String getErrorMessage() { return errorMessage; }

    public static class Builder {
        private boolean success = true;
        private String resultText = "";
        private Map<String, Object> data = new HashMap<>();
        private long executionTimeMs = 0;
        private String errorMessage;

        public Builder success(boolean s) { this.success = s; return this; }
        public Builder resultText(String text) { this.resultText = text; return this; }
        public Builder data(Map<String, Object> d) { this.data = d; return this; }
        public Builder executionTime(long ms) { this.executionTimeMs = ms; return this; }
        public Builder error(String msg) {
            this.success = false;
            this.errorMessage = msg;
            return this;
        }

        public ToolResult build() {
            return new ToolResult(this);
        }
    }
}
