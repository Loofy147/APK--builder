package com.jomra.ai.agents;

import java.util.Collection;

public class AgentSelector {
    public Agent selectAgent(AgentInput input, Collection<Agent> agents) {
        String text = input.getText().toLowerCase();
        AgentCapability targetCapability;

        if (text.contains("calculate") || text.contains("math")) {
            targetCapability = AgentCapability.TOOL_USAGE;
        } else if (text.contains("learn") || text.contains("remember")) {
            targetCapability = AgentCapability.REINFORCEMENT_LEARNING;
        } else if (text.contains("search") || text.contains("find")) {
            targetCapability = AgentCapability.TOOL_USAGE;
        } else if (text.contains("github") || text.contains("repo") || text.contains("code")) {
            targetCapability = AgentCapability.TOOL_USAGE; // GitHub Connector
        } else if (text.contains("supabase") || text.contains("db") || text.contains("database")) {
            targetCapability = AgentCapability.TOOL_USAGE; // Supabase Connector
        } else if (text.contains("vercel") || text.contains("deploy")) {
            targetCapability = AgentCapability.TOOL_USAGE; // Vercel Connector
        } else {
            targetCapability = AgentCapability.QUESTION_ANSWERING;
        }

        return findStrategicAgent(agents, targetCapability);
    }

    /**
     * SUN-TZU: Strategic selection prioritizing HEALTHY agents.
     * "The wise warrior avoids the battle." - By picking healthy agents, we avoid failures.
     */
    private Agent findStrategicAgent(Collection<Agent> agents, AgentCapability capability) {
        Agent bestAgent = null;

        for (Agent agent : agents) {
            if (hasCapability(agent, capability)) {
                HealthStatus.Status currentStatus = agent.getHealthStatus().getStatus();

                if (bestAgent == null) {
                    bestAgent = agent;
                    continue;
                }

                HealthStatus.Status bestStatus = bestAgent.getHealthStatus().getStatus();

                // Prioritize Healthy over anything else
                if (currentStatus == HealthStatus.Status.HEALTHY && bestStatus != HealthStatus.Status.HEALTHY) {
                    bestAgent = agent;
                }
                // If both are Healthy, pick the one with lower latency
                else if (currentStatus == HealthStatus.Status.HEALTHY && bestStatus == HealthStatus.Status.HEALTHY) {
                    if (agent.getEstimatedLatencyMs() < bestAgent.getEstimatedLatencyMs()) {
                        bestAgent = agent;
                    }
                }
                // If current is DEGRADED but best is UNHEALTHY
                else if (currentStatus == HealthStatus.Status.DEGRADED && bestStatus == HealthStatus.Status.UNHEALTHY) {
                    bestAgent = agent;
                }
            }
        }

        return bestAgent != null ? bestAgent : (agents.isEmpty() ? null : agents.iterator().next());
    }

    private boolean hasCapability(Agent agent, AgentCapability capability) {
        for (AgentCapability cap : agent.getCapabilities()) {
            if (cap == capability) return true;
        }
        return false;
    }
}
