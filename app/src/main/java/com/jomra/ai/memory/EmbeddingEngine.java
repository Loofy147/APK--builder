package com.jomra.ai.memory;

public class EmbeddingEngine {
    private static final int EMBEDDING_DIM = 128;
    public float[] encode(String text) {
        float[] embedding = new float[EMBEDDING_DIM];
        int hash = text.hashCode();
        for (int i = 0; i < EMBEDDING_DIM; i++) {
            embedding[i] = (float) Math.sin(hash * (i + 1)) * 0.1f;
        }
        return embedding;
    }
}
