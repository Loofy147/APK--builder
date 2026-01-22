package com.jomra.ai.agents;

import android.util.Log;
import java.util.*;
import java.util.concurrent.*;

public class AgentOrchestrator {
    private static final String TAG = "AgentOrchestrator";
    private static final long DEFAULT_TIMEOUT_MS = 30000;

    private final Map<String, Agent> agents;
    private final ExecutorService executor;
    private final AgentSelector selector;

    public AgentOrchestrator() {
        this.agents = new ConcurrentHashMap<>();
        this.executor = Executors.newFixedThreadPool(3);
        this.selector = new AgentSelector();
    }

    public void registerAgent(Agent agent) {
        if (agent.initialize()) {
            agents.put(agent.getId(), agent);
            Log.i(TAG, "Registered agent: " + agent.getName());
        } else {
            Log.e(TAG, "Failed to initialize agent: " + agent.getName());
        }
    }

    public AgentResponse processSingle(AgentContext context, String userInput) {
        AgentInput input = new AgentInput(userInput,
            AgentInput.InputType.TEXT, Collections.emptyMap());
        Agent selectedAgent = selector.selectAgent(input, agents.values());
        if (selectedAgent == null) {
            return AgentResponse.error("No suitable agent found");
        }
        Log.i(TAG, "Selected agent: " + selectedAgent.getName());
        return executeWithTimeout(selectedAgent, context, input);
    }

    public AgentResponse processEnsemble(AgentContext context, String userInput) {
        AgentInput input = new AgentInput(userInput,
            AgentInput.InputType.TEXT, Collections.emptyMap());
        List<Future<AgentResponse>> futures = new ArrayList<>();
        for (Agent agent : agents.values()) {
            Future<AgentResponse> future = executor.submit(() -> {
                try {
                    return agent.process(context, input);
                } catch (Exception e) {
                    Log.e(TAG, "Agent error: " + agent.getName(), e);
                    return AgentResponse.error(e.getMessage());
                }
            });
            futures.add(future);
        }
        List<AgentResponse> responses = new ArrayList<>();
        for (Future<AgentResponse> future : futures) {
            try {
                AgentResponse response = future.get(15, TimeUnit.SECONDS);
                if (response.isSuccess()) {
                    responses.add(response);
                }
            } catch (TimeoutException e) {
                future.cancel(true);
                Log.w(TAG, "Agent timed out");
            } catch (Exception e) {
                Log.e(TAG, "Error getting agent response", e);
            }
        }
        return aggregateResponses(responses);
    }

    public AgentResponse processPipeline(AgentContext context,
                                        String userInput,
                                        String[] agentOrder) {
        AgentInput input = new AgentInput(userInput,
            AgentInput.InputType.TEXT, Collections.emptyMap());
        AgentResponse currentResponse = null;
        for (String agentId : agentOrder) {
            Agent agent = agents.get(agentId);
            if (agent == null) {
                Log.w(TAG, "Agent not found in pipeline: " + agentId);
                continue;
            }
            if (currentResponse != null && currentResponse.isSuccess()) {
                input = new AgentInput(currentResponse.getText(),
                    AgentInput.InputType.TEXT, currentResponse.getMetadata());
            }
            currentResponse = executeWithTimeout(agent, context, input);
            if (!currentResponse.isSuccess()) {
                Log.w(TAG, "Pipeline stopped at agent: " + agent.getName());
                break;
            }
        }
        return currentResponse != null ? currentResponse :
            AgentResponse.error("Pipeline failed");
    }

    private AgentResponse executeWithTimeout(Agent agent,
                                            AgentContext context,
                                            AgentInput input) {
        Future<AgentResponse> future = executor.submit(() -> {
            try {
                return agent.process(context, input);
            } catch (AgentException e) {
                Log.e(TAG, "Agent exception: " + agent.getName(), e);
                return AgentResponse.error("Agent error: " + e.getMessage());
            }
        });
        try {
            long timeout = agent.getEstimatedLatencyMs() * 3;
            timeout = Math.max(timeout, 5000);
            timeout = Math.min(timeout, DEFAULT_TIMEOUT_MS);
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            Log.e(TAG, "Agent timeout: " + agent.getName());
            return AgentResponse.timeout();
        } catch (Exception e) {
            Log.e(TAG, "Execution error", e);
            return AgentResponse.error("Execution failed: " + e.getMessage());
        }
    }

    private AgentResponse aggregateResponses(List<AgentResponse> responses) {
        if (responses.isEmpty()) {
            return AgentResponse.error("No successful responses");
        }
        if (responses.size() == 1) {
            return responses.get(0);
        }
        AgentResponse best = responses.get(0);
        for (AgentResponse response : responses) {
            if (response.getConfidence() > best.getConfidence()) {
                best = response;
            }
        }
        Map<String, Object> meta = new HashMap<>();
        meta.put("ensemble_size", responses.size());

        List<Map<String, Object>> responseList = new ArrayList<>();
        for (AgentResponse r : responses) {
            Map<String, Object> rMeta = new HashMap<>();
            rMeta.put("text", r.getText());
            rMeta.put("confidence", r.getConfidence());
            responseList.add(rMeta);
        }
        meta.put("responses", responseList);

        return new AgentResponse.Builder()
            .status(best.getStatus())
            .text(best.getText())
            .confidence(best.getConfidence())
            .metadata(meta)
            .build();
    }

    public Map<String, HealthStatus> getAgentHealth() {
        Map<String, HealthStatus> health = new HashMap<>();
        for (Map.Entry<String, Agent> entry : agents.entrySet()) {
            health.put(entry.getKey(), entry.getValue().getHealthStatus());
        }
        return health;
    }

    public void shutdown() {
        Log.i(TAG, "Shutting down orchestrator...");
        for (Agent agent : agents.values()) {
            try {
                agent.shutdown();
            } catch (Exception e) {
                Log.e(TAG, "Error shutting down agent", e);
            }
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
