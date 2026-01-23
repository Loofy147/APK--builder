package com.jomra.ai.agents.advanced;

import android.content.Context;
import com.jomra.ai.agents.*;
import com.jomra.ai.moaziz.MoazizOrchestrator;
import com.jomra.ai.memory.MemorySystem;
import java.util.Collections;

public class MoazizAgent implements Agent {
    private final MoazizOrchestrator orchestrator;
    private boolean initialized = false;

    public MoazizAgent(Context context, MemorySystem memory) {
        this.orchestrator = new MoazizOrchestrator(context, memory);
    }

    public void registerInternalAgent(Agent agent) {
        orchestrator.registerAgent(agent);
    }

    @Override public String getId() { return "moaziz_agent"; }
    @Override public String getName() { return "Moaziz Supreme Agent"; }
    @Override public AgentCapability[] getCapabilities() {
        return new AgentCapability[]{
            AgentCapability.REASONING,
            AgentCapability.PLANNING,
            AgentCapability.QUESTION_ANSWERING
        };
    }

    @Override
    public boolean initialize() {
        initialized = true;
        return true;
    }

    @Override
    public AgentResponse process(AgentContext context, AgentInput input) throws AgentException {
        return orchestrator.process(context, input.getText());
    }

    @Override public void shutdown() { initialized = false; }
    @Override public HealthStatus getHealthStatus() { return HealthStatus.healthy(); }
    @Override public long getEstimatedLatencyMs() { return 2000; }
}
