package com.jomra.ai.memory;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.jomra.ai.storage.AppDatabase;
import com.jomra.ai.storage.SecureStorage;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Persistent Memory System for Agents.
 */
public class MemorySystem {
    private static final String TAG = "MemorySystem";
    private static final int SHORT_TERM_CAPACITY = 10;
    private static final float MEMORY_RETENTION_THRESHOLD = 0.3f;

    private final Context context;
    private final SecureStorage secureStorage;
    private final MemoryDao memoryDao;
    private final Queue<MemoryItem> shortTermMemory;
    private final Map<String, Float> userPreferences;
    private final EmbeddingEngine embeddingEngine;
    private final ExecutorService diskExecutor;
    private final Gson gson;

    public MemorySystem(Context context) {
        this.context = context.getApplicationContext();
        this.secureStorage = new SecureStorage(context);
        this.memoryDao = AppDatabase.getDatabase(context).memoryDao();
        this.shortTermMemory = new LinkedList<>();
        this.userPreferences = new ConcurrentHashMap<>();
        this.embeddingEngine = new EmbeddingEngine();
        this.diskExecutor = Executors.newSingleThreadExecutor();
        this.gson = new Gson();

        loadUserPreferences();
        loadMemoriesFromDisk();
    }

    private void loadMemoriesFromDisk() {
        diskExecutor.execute(() -> {
            try {
                List<MemoryEntity> entities = memoryDao.getRecent(SHORT_TERM_CAPACITY);
                synchronized (shortTermMemory) {
                    shortTermMemory.clear();
                    for (int i = entities.size() - 1; i >= 0; i--) {
                        shortTermMemory.offer(entityToItem(entities.get(i)));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading memories", e);
            }
        });
    }

    public void remember(String userInput, String agentResponse,
                        float importance, Map<String, Object> metadata) {

        MemoryItem item = new MemoryItem(
            UUID.randomUUID().toString(),
            userInput,
            agentResponse,
            System.currentTimeMillis(),
            importance,
            metadata
        );
        item.embedding = embeddingEngine.encode(userInput);

        synchronized (shortTermMemory) {
            shortTermMemory.offer(item);
            if (shortTermMemory.size() > SHORT_TERM_CAPACITY) {
                shortTermMemory.poll();
            }
        }

        if (importance > MEMORY_RETENTION_THRESHOLD) {
            diskExecutor.execute(() -> memoryDao.insert(itemToEntity(item)));
        }

        updatePreferences(item);
    }

    public List<MemoryItem> recall(String query, int topK) {
        List<MemoryItem> allMemories = new ArrayList<>();

        // Safety: ensure we are not on main thread if database is potentially large.
        // In this implementation, we still do a synchronous load for agent reasoning.
        // Agents should ideally call this from a background thread.
        try {
            List<MemoryEntity> entities = memoryDao.getAll();
            for (MemoryEntity entity : entities) {
                allMemories.add(entityToItem(entity));
            }
        } catch (Exception e) {
            Log.e(TAG, "Recall failed", e);
        }

        float[] queryEmbedding = embeddingEngine.encode(query);
        for (MemoryItem item : allMemories) {
            if (item.embedding != null) {
                item.relevanceScore = cosineSimilarity(queryEmbedding, item.embedding);
            }
        }

        Collections.sort(allMemories, (a, b) -> Float.compare(b.relevanceScore, a.relevanceScore));

        return allMemories.subList(0, Math.min(topK, allMemories.size()));
    }

    private MemoryEntity itemToEntity(MemoryItem item) {
        return new MemoryEntity(item.id, item.userInput, item.agentResponse,
                               item.timestamp, item.importance,
                               gson.toJson(item.metadata), gson.toJson(item.embedding));
    }

    private MemoryItem entityToItem(MemoryEntity entity) {
        Map<String, Object> metadata = gson.fromJson(entity.metadataJson, Map.class);
        float[] embedding = gson.fromJson(entity.embeddingJson, float[].class);
        MemoryItem item = new MemoryItem(entity.id, entity.userInput, entity.agentResponse,
                             entity.timestamp, entity.importance, metadata);
        item.embedding = embedding;
        return item;
    }

    private float cosineSimilarity(float[] a, float[] b) {
        float dotProduct = 0f;
        float normA = 0f;
        float normB = 0f;
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0;
        return dotProduct / (float) (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private void updatePreferences(MemoryItem item) {
        Map<String, Object> meta = item.metadata;
        if (meta != null && "positive".equals(meta.get("user_feedback"))) {
            String category = (String) meta.getOrDefault("category", "general");
            float current = userPreferences.getOrDefault(category, 0.5f);
            userPreferences.put(category, Math.min(1.0f, current + 0.1f));
            saveUserPreferences();
        }
    }

    private void saveUserPreferences() {
        for (Map.Entry<String, Float> entry : userPreferences.entrySet()) {
            secureStorage.putString("pref_" + entry.getKey(), String.valueOf(entry.getValue()));
        }
    }

    private void loadUserPreferences() {
        String[] categories = {"language", "vision", "tools", "rl"};
        for (String category : categories) {
            String value = secureStorage.getString("pref_" + category, "0.5");
            userPreferences.put(category, Float.parseFloat(value));
        }
    }

    public void clearAll() {
        synchronized (shortTermMemory) {
            shortTermMemory.clear();
        }
        diskExecutor.execute(memoryDao::deleteAll);
        userPreferences.clear();
    }
}
