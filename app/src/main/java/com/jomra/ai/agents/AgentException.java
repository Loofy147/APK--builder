package com.jomra.ai.agents;

public class AgentException extends Exception {
    public enum ErrorType {
        MODEL_LOAD_FAILED,
        INFERENCE_ERROR,
        INVALID_INPUT,
        TIMEOUT,
        RESOURCE_EXHAUSTED,
        CONFIGURATION_ERROR
    }

    private final ErrorType errorType;

    public AgentException(ErrorType type, String message) {
        super(message);
        this.errorType = type;
    }

    public AgentException(ErrorType type, String message, Throwable cause) {
        super(message, cause);
        this.errorType = type;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
