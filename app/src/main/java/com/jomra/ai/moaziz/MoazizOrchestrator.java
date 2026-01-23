package com.jomra.ai.moaziz;

import android.content.Context;
import android.util.Log;
import com.jomra.ai.agents.*;
import com.jomra.ai.memory.MemorySystem;
import com.jomra.ai.memory.MemoryItem;
import java.util.*;

/**
 * Moaziz-style Advanced Orchestrator.
 * Implements MAWO (Multi-Agent Workflow), BAP (Bounded Autonomy), and AAR (Adaptive Agent Routing).
 */
public class MoazizOrchestrator {
    private static final String TAG = "MoazizOrchestrator";

    private final MoazizPolicy policy;
    private final AdversarialTrinity security;
    private final HICRAEngine hicra;
    private final MemorySystem memory;
    private final Map<String, Agent> registeredAgents = new HashMap<>();

    // Performance metrics for AAR
    private final Map<String, Float> avgLatency = new HashMap<>();
    private final Map<String, Float> avgAccuracy = new HashMap<>();

    public MoazizOrchestrator(Context context, MemorySystem memory) {
        this.policy = new MoazizPolicy(context);
        this.security = new AdversarialTrinity();
        this.hicra = new HICRAEngine();
        this.memory = memory;
    }

    public void registerAgent(Agent agent) {
        registeredAgents.put(agent.getId(), agent);
        avgLatency.put(agent.getId(), (float) agent.getEstimatedLatencyMs());
        avgAccuracy.put(agent.getId(), 0.9f);
    }

    public AgentResponse process(AgentContext context, String userInput) {
        // 1. Security Screening (L6 Guard)
        AdversarialTrinity.ScreeningResult securityResult = security.screenPrompt(userInput);
        if (!securityResult.safe) {
            return AgentResponse.error("Security Violation: " + securityResult.reason);
        }

        // 2. Context Retrieval (L3 Relational State)
        List<MemoryItem> relatedMemories = memory.recall(userInput, 3);
        StringBuilder enrichedInput = new StringBuilder(userInput);
        if (!relatedMemories.isEmpty()) {
            enrichedInput.append("\n\nContext from memory:");
            for (MemoryItem item : relatedMemories) {
                enrichedInput.append("\n- User: ").append(item.userInput).append("\n  Agent: ").append(item.agentResponse);
            }
        }
        String finalInputText = enrichedInput.toString();

        // 3. Adaptive Agent Routing (AAR / L2-L3)
        List<String> selectedAgentIds = selectAgents(userInput);
        if (selectedAgentIds.isEmpty()) {
            return AgentResponse.error("No suitable agents found for this task");
        }

        // 3. Multi-Agent Workflow (MAWO / L1-L4)
        // Sequential execution with Bounded Autonomy (BAP)
        AgentResponse finalResponse = null;
        float currentConfidence = 1.0f;
        int stepCount = 0;

        for (String agentId : selectedAgentIds) {
            Agent agent = registeredAgents.get(agentId);
            if (agent == null) continue;

            // Bounded Autonomy Check
            if (stepCount >= 5 || currentConfidence < 0.7f) {
                Log.w(TAG, "Escalation: Confidence dropped or max steps reached");
                break;
            }

            long start = System.currentTimeMillis();
            try {
                AgentInput input = new AgentInput(finalInputText, AgentInput.InputType.TEXT, Collections.emptyMap());
                AgentResponse response = agent.process(context, input);

                long latency = System.currentTimeMillis() - start;
                updateMetrics(agentId, latency, response.getConfidence());

                if (response.isSuccess()) {
                    finalResponse = response;
                    currentConfidence = response.getConfidence();
                }
            } catch (Exception e) {
                Log.e(TAG, "Agent execution failed: " + agentId, e);
            }
            stepCount++;
        }

        return finalResponse != null ? finalResponse : AgentResponse.error("Workflow failed");
    }

    private List<String> selectAgents(String query) {
        // Simple AAR logic based on keyword matching and performance
        List<String> selection = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        // New Logic: Direct model capability mapping
        if (lowerQuery.contains("code") || lowerQuery.contains("program")) {
            if (registeredAgents.containsKey("mistral_agent")) selection.add("mistral_agent");
        }
        if (lowerQuery.contains("analyze") || lowerQuery.contains("reason")) {
            selection.add("chain_of_thought");
        }

        // Consult learned policy
        Map<String, Object> action = policy.getAction("env_coordination", "medium", "high_perf");
        if (action != null && action.containsKey("best_agents")) {
            Object bestObj = action.get("best_agents");
            if (bestObj instanceof List) {
                List<?> best = (List<?>) bestObj;
                for (Object idObj : best) {
                    String id = String.valueOf(idObj);
                    // Map Moaziz IDs to Jomra IDs
                    if (id.equals("writer")) selection.add("qa_agent");
                    if (id.equals("researcher")) selection.add("tool_agent");
                    if (id.equals("analyst") && registeredAgents.containsKey("chain_of_thought")) selection.add("chain_of_thought");
                }
            }
        }

        // Fallback to Jomra agents
        if (selection.isEmpty()) {
            selection.add("qa_agent");
            if (query.contains("+") || query.contains("-")) selection.add("tool_agent");
        }

        return selection;
    }

    private void updateMetrics(String agentId, long latency, float accuracy) {
        float alpha = 0.2f;
        float currentLat = avgLatency.getOrDefault(agentId, 1000f);
        float currentAcc = avgAccuracy.getOrDefault(agentId, 0.9f);

        avgLatency.put(agentId, (1 - alpha) * currentLat + alpha * latency);
        avgAccuracy.put(agentId, (1 - alpha) * currentAcc + alpha * accuracy);
    }
}
