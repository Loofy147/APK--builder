package com.jomra.ai.agents;

public interface Agent {
    String getId();
    String getName();
    AgentCapability[] getCapabilities();
    AgentResponse process(AgentContext context, AgentInput input) throws AgentException;
    boolean initialize();
    void shutdown();
    HealthStatus getHealthStatus();
    long getEstimatedLatencyMs();
}
