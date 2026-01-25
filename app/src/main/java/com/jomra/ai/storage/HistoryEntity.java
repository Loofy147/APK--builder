package com.jomra.ai.storage;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * TUBER: Persistent Conversation History Entity.
 */
@Entity(tableName = "conversation_history")
public class HistoryEntity {
    @PrimaryKey
    @NonNull
    public String id;
    public String userInput;
    public String agentResponse;
    public long timestamp;

    public HistoryEntity(@NonNull String id, String userInput, String agentResponse, long timestamp) {
        this.id = id;
        this.userInput = userInput;
        this.agentResponse = agentResponse;
        this.timestamp = timestamp;
    }
}
