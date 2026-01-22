package com.jomra.ai.agents;

import java.util.Collection;

public class AgentSelector {
    public Agent selectAgent(AgentInput input, Collection<Agent> agents) {
        String text = input.getText().toLowerCase();
        if (text.contains("calculate") || text.contains("math")) {
            return findByCapability(agents, AgentCapability.TOOL_USAGE);
        } else if (text.contains("learn") || text.contains("remember")) {
            return findByCapability(agents, AgentCapability.REINFORCEMENT_LEARNING);
        } else if (text.contains("search") || text.contains("find")) {
            return findByCapability(agents, AgentCapability.TOOL_USAGE);
        } else {
            return findByCapability(agents, AgentCapability.QUESTION_ANSWERING);
        }
    }

    private Agent findByCapability(Collection<Agent> agents,
                                   AgentCapability capability) {
        for (Agent agent : agents) {
            for (AgentCapability cap : agent.getCapabilities()) {
                if (cap == capability) {
                    return agent;
                }
            }
        }
        return agents.isEmpty() ? null : agents.iterator().next();
    }
}
