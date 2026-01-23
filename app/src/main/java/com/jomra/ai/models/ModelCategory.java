package com.jomra.ai.models;

public enum ModelCategory {
    LANGUAGE("Language Models", "NLP, QA, Translation, Summarization"),
    VISION("Vision Models", "Image classification, Object detection, OCR"),
    AUDIO("Audio Models", "Speech recognition, TTS, Audio classification"),
    MULTIMODAL("Multimodal", "Vision + Language, Cross-modal understanding"),
    REINFORCEMENT_LEARNING("RL Models", "Decision making, Game playing, Control"),
    EMBEDDING("Embedding Models", "Text/Image embeddings, Similarity search"),
    GENERATIVE("Generative Models", "Image/Text generation, Style transfer"),
    SPECIALIZED("Specialized", "Domain-specific models");

    public final String displayName;
    public final String description;

    ModelCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
