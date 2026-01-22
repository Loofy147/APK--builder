package com.jomra.ai.agents;

import java.util.ArrayList;
import java.util.List;

public class ConversationHistory {
    private final List<ConversationTurn> turns;
    private final int maxTurns;

    public ConversationHistory(int maxTurns) {
        this.turns = new ArrayList<>();
        this.maxTurns = maxTurns;
    }

    public void addTurn(String userInput, String agentResponse) {
        turns.add(new ConversationTurn(userInput, agentResponse));
        while (turns.size() > maxTurns) {
            turns.remove(0);
        }
    }

    public List<ConversationTurn> getTurns() {
        return new ArrayList<>(turns);
    }

    public static class ConversationTurn {
        public final String userInput;
        public final String agentResponse;
        public final long timestamp;

        public ConversationTurn(String input, String response) {
            this.userInput = input;
            this.agentResponse = response;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
