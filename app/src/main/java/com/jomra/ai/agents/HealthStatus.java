package com.jomra.ai.agents;

import java.util.Map;

public class HealthStatus {
    public enum Status {
        HEALTHY, DEGRADED, UNHEALTHY, INITIALIZING
    }

    private final Status status;
    private final String message;
    private final Map<String, Object> metrics;

    public HealthStatus(Status status, String message, Map<String, Object> metrics) {
        this.status = status;
        this.message = message;
        this.metrics = metrics;
    }

    public Status getStatus() { return status; }
    public String getMessage() { return message; }
    public Map<String, Object> getMetrics() { return metrics; }

    public static HealthStatus healthy() {
        return new HealthStatus(Status.HEALTHY, "Agent operational", Map.of());
    }

    public static HealthStatus unhealthy(String reason) {
        return new HealthStatus(Status.UNHEALTHY, reason, Map.of());
    }
}
