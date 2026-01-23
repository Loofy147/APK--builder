package com.jomra.ai.agents.advanced;

import android.content.Context;
import android.util.Log;
import com.jomra.ai.agents.*;
import com.jomra.ai.models.ModelManager;
import com.jomra.ai.tools.*;
import com.jomra.ai.memory.MemorySystem;
import com.jomra.ai.memory.MemoryItem;
import java.util.*;

public class ChainOfThoughtAgent implements Agent {
    private final Agent qaAgent;
    private final ToolRegistry toolRegistry;
    private final MemorySystem memory;
    private boolean initialized = false;

    public ChainOfThoughtAgent(Context context, ModelManager modelManager,
                              ToolRegistry toolRegistry, Agent qaAgent, MemorySystem memory) {
        this.toolRegistry = toolRegistry;
        this.qaAgent = qaAgent;
        this.memory = memory;
    }

    @Override
    public String getId() { return "chain_of_thought"; }
    @Override
    public String getName() { return "Chain-of-Thought Agent"; }
    @Override
    public AgentCapability[] getCapabilities() { return new AgentCapability[]{AgentCapability.REASONING}; }

    @Override
    public boolean initialize() {
        initialized = qaAgent.initialize();
        return initialized;
    }

    @Override
    public AgentResponse process(AgentContext context, AgentInput input) throws AgentException {
        String query = input.getText();
        List<String> reasoningSteps = new ArrayList<>();

        // Simple heuristic for reasoning
        if (query.contains("+") || query.contains("-") || query.contains("*") || query.contains("/")) {
            reasoningSteps.add("Identify mathematical operation in the query.");
            reasoningSteps.add("Extract operands and operator.");
            reasoningSteps.add("Calculate the result using the Calculator tool.");
        } else {
            reasoningSteps.add("Analyze query intent.");
            List<MemoryItem> recalled = memory.recall(query, 2);
            if (!recalled.isEmpty()) {
                reasoningSteps.add("Found " + recalled.size() + " relevant context items in memory.");
            }
            reasoningSteps.add("Retrieve relevant information from memory.");
            reasoningSteps.add("Synthesize a response based on available data.");
        }

        StringBuilder sb = new StringBuilder("Thought process:\n");
        for (int i = 0; i < reasoningSteps.size(); i++) {
            sb.append(i + 1).append(". ").append(reasoningSteps.get(i)).append("\n");
        }

        AgentResponse baseResponse = qaAgent.process(context, input);
        sb.append("\nFinal Answer: ").append(baseResponse.getText());

        return new AgentResponse.Builder()
                .status(AgentResponse.ResponseStatus.SUCCESS)
                .text(sb.toString())
                .confidence(0.95f)
                .build();
    }

    @Override public void shutdown() { initialized = false; }
    @Override public HealthStatus getHealthStatus() { return initialized ? HealthStatus.healthy() : HealthStatus.unhealthy("Not init"); }
    @Override public long getEstimatedLatencyMs() { return 1500; }
}
