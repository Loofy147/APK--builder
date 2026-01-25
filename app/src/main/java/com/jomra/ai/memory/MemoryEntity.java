package com.jomra.ai.memory;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Index;
import androidx.annotation.NonNull;

/**
 * TUBER: Optimized Memory Entity with indices for faster retrieval.
 */
@Entity(tableName = "memories",
        indices = {@Index("timestamp"), @Index("importance")})
public class MemoryEntity {
    @PrimaryKey
    @NonNull
    public String id;
    public String userInput;
    public String agentResponse;
    public long timestamp;
    public float importance;
    public String metadataJson; // Store metadata as JSON string
    public String embeddingJson; // Store embedding as JSON string

    public MemoryEntity(@NonNull String id, String userInput, String agentResponse,
                        long timestamp, float importance, String metadataJson, String embeddingJson) {
        this.id = id;
        this.userInput = userInput;
        this.agentResponse = agentResponse;
        this.timestamp = timestamp;
        this.importance = importance;
        this.metadataJson = metadataJson;
        this.embeddingJson = embeddingJson;
    }
}
