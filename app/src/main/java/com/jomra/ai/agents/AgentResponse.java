package com.jomra.ai.agents;

import java.util.Collections;
import java.util.Map;

public class AgentResponse {
    private final ResponseStatus status;
    private final String text;
    private final float confidence;
    private final Map<String, Object> metadata;
    private final Action suggestedAction;
    private final String errorMessage;

    private AgentResponse(Builder builder) {
        this.status = builder.status;
        this.text = builder.text;
        this.confidence = builder.confidence;
        this.metadata = builder.metadata;
        this.suggestedAction = builder.action;
        this.errorMessage = builder.errorMessage;
    }

    public ResponseStatus getStatus() { return status; }
    public String getText() { return text; }
    public float getConfidence() { return confidence; }
    public Map<String, Object> getMetadata() { return metadata; }
    public Action getSuggestedAction() { return suggestedAction; }
    public String getErrorMessage() { return errorMessage; }

    public boolean isSuccess() {
        return status == ResponseStatus.SUCCESS;
    }

    public enum ResponseStatus {
        SUCCESS, TIMEOUT, ERROR, FALLBACK, INSUFFICIENT_CONFIDENCE
    }

    public static class Builder {
        private ResponseStatus status = ResponseStatus.SUCCESS;
        private String text = "";
        private float confidence = 0.0f;
        private Map<String, Object> metadata = Collections.emptyMap();
        private Action action;
        private String errorMessage;

        public Builder status(ResponseStatus status) {
            this.status = status;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder confidence(float conf) {
            this.confidence = Math.max(0.0f, Math.min(1.0f, conf));
            return this;
        }

        public Builder metadata(Map<String, Object> meta) {
            this.metadata = meta;
            return this;
        }

        public Builder action(Action action) {
            this.action = action;
            return this;
        }

        public Builder error(String message) {
            this.status = ResponseStatus.ERROR;
            this.errorMessage = message;
            return this;
        }

        public AgentResponse build() {
            return new AgentResponse(this);
        }
    }

    public static AgentResponse success(String text, float confidence) {
        return new Builder()
            .status(ResponseStatus.SUCCESS)
            .text(text)
            .confidence(confidence)
            .build();
    }

    public static AgentResponse error(String message) {
        return new Builder()
            .status(ResponseStatus.ERROR)
            .error(message)
            .build();
    }

    public static AgentResponse timeout() {
        return new Builder()
            .status(ResponseStatus.TIMEOUT)
            .text("Agent processing timed out")
            .build();
    }
}
