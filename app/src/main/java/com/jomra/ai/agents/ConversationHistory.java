package com.jomra.ai.agents;

import com.jomra.ai.utils.TokenUtils;
import java.util.ArrayList;
import java.util.List;

public class ConversationHistory {
    private final List<ConversationTurn> turns;
    private final int maxTurns;
    private static final int MAX_TOKENS = 4096; // Example limit

    public ConversationHistory(int maxTurns) {
        this.turns = new ArrayList<>();
        this.maxTurns = maxTurns;
    }

    public void addTurn(String userInput, String agentResponse) {
        turns.add(new ConversationTurn(userInput, agentResponse));
        optimizeContext();
    }

    /**
     * BOLT: Token optimization - sliding window context management.
     */
    private void optimizeContext() {
        // 1. Limit by turn count
        while (turns.size() > maxTurns) {
            turns.remove(0);
        }

        // 2. Limit by token count
        while (getTotalTokens() > MAX_TOKENS && !turns.isEmpty()) {
            turns.remove(0);
        }
    }

    private int getTotalTokens() {
        int total = 0;
        for (ConversationTurn turn : turns) {
            total += turn.getTokens(); // BOLT: Use cached tokens
        }
        return total;
    }

    public List<ConversationTurn> getTurns() {
        return new ArrayList<>(turns);
    }

    public static class ConversationTurn {
        public final String userInput;
        public final String agentResponse;
        public final long timestamp;
        private final int tokens; // BOLT: Cached token count

        public ConversationTurn(String input, String response) {
            this.userInput = input;
            this.agentResponse = response;
            this.timestamp = System.currentTimeMillis();
            // BOLT: Pre-calculate tokens on creation
            this.tokens = TokenUtils.estimateTokens(input) + TokenUtils.estimateTokens(response);
        }

        public int getTokens() { return tokens; }
    }
}
