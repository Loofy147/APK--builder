package com.jomra.ai.utils;

public class TokenUtils {
    /**
     * Estimates token count for a given text.
     * Simple approximation: 1 token ~= 4 characters for English.
     */
    public static int estimateTokens(String text) {
        if (text == null) return 0;
        return (int) Math.ceil(text.length() / 4.0);
    }

    /**
     * Checks if the text exceeds a token limit.
     */
    public static boolean exceedsLimit(String text, int limit) {
        return estimateTokens(text) > limit;
    }
}
