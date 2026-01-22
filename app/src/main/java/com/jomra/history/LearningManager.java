package com.jomra.history;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages learning progress with proper Spaced Repetition System (SRS)
 * Memory-safe: Uses ApplicationContext to prevent leaks
 */
public class LearningManager {
    private static final String PREFS_NAME = "JomraProgress";
    private static final String KEY_LEVEL = "current_level";
    private static final String PREFIX_NEXT_REVIEW = "next_review_";
    private static final String PREFIX_INTERVAL = "interval_";
    private static final String PREFIX_EASE = "ease_";

    // SRS intervals in milliseconds
    private static final long INTERVAL_NEW = 0;
    private static final long INTERVAL_LEARNING = 10 * 60 * 1000; // 10 min
    private static final long INTERVAL_YOUNG = 24 * 60 * 60 * 1000; // 1 day
    private static final long INTERVAL_MATURE_BASE = 3 * 24 * 60 * 60 * 1000; // 3 days

    // Ease factors
    private static final float EASE_DEFAULT = 2.5f;
    private static final float EASE_MIN = 1.3f;
    private static final float EASE_BONUS = 0.15f;
    private static final float EASE_PENALTY = 0.2f;

    private final SharedPreferences prefs;
    private int currentLevel;

    // Cache for performance
    private final Map<Integer, Long> nextReviewCache;
    private final Map<Integer, Long> intervalCache;
    private final Map<Integer, Float> easeCache;

    public LearningManager(Context context) {
        // Use application context to prevent memory leaks
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.currentLevel = prefs.getInt(KEY_LEVEL, 1);
        this.nextReviewCache = new HashMap<>();
        this.intervalCache = new HashMap<>();
        this.easeCache = new HashMap<>();
        loadCache();
    }

    private void loadCache() {
        Map<String, ?> all = prefs.getAll();
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(PREFIX_NEXT_REVIEW)) {
                int id = Integer.parseInt(key.substring(PREFIX_NEXT_REVIEW.length()));
                nextReviewCache.put(id, (Long) entry.getValue());
            } else if (key.startsWith(PREFIX_INTERVAL)) {
                int id = Integer.parseInt(key.substring(PREFIX_INTERVAL.length()));
                intervalCache.put(id, (Long) entry.getValue());
            } else if (key.startsWith(PREFIX_EASE)) {
                int id = Integer.parseInt(key.substring(PREFIX_EASE.length()));
                easeCache.put(id, (Float) entry.getValue());
            }
        }
    }

    /**
     * Record answer and update SRS schedule
     * @param questionId Question ID
     * @param correct Whether answer was correct
     */
    public void recordAnswer(int questionId, boolean correct) {
        long now = System.currentTimeMillis();
        long currentInterval = intervalCache.getOrDefault(questionId, INTERVAL_NEW);
        float currentEase = easeCache.getOrDefault(questionId, EASE_DEFAULT);

        long newInterval;
        float newEase = currentEase;

        if (correct) {
            // Correct answer: increase interval
            if (currentInterval == INTERVAL_NEW) {
                newInterval = INTERVAL_LEARNING;
            } else if (currentInterval == INTERVAL_LEARNING) {
                newInterval = INTERVAL_YOUNG;
            } else {
                // Apply ease factor
                newInterval = (long) (currentInterval * currentEase);
                newEase = Math.min(currentEase + EASE_BONUS, 3.0f);
            }
        } else {
            // Wrong answer: reset to learning, reduce ease
            newInterval = INTERVAL_LEARNING;
            newEase = Math.max(currentEase - EASE_PENALTY, EASE_MIN);
        }

        long nextReview = now + newInterval;

        // Update cache
        nextReviewCache.put(questionId, nextReview);
        intervalCache.put(questionId, newInterval);
        easeCache.put(questionId, newEase);

        // Persist (async to avoid blocking UI)
        saveAsync(questionId, nextReview, newInterval, newEase);
    }

    private void saveAsync(int questionId, long nextReview, long interval, float ease) {
        new Thread(() -> {
            prefs.edit()
                .putLong(PREFIX_NEXT_REVIEW + questionId, nextReview)
                .putLong(PREFIX_INTERVAL + questionId, interval)
                .putFloat(PREFIX_EASE + questionId, ease)
                .apply();
        }).start();
    }

    /**
     * Check if question is mastered (mature card)
     */
    public boolean isMastered(int questionId) {
        long interval = intervalCache.getOrDefault(questionId, INTERVAL_NEW);
        return interval >= INTERVAL_MATURE_BASE;
    }

    /**
     * Get due questions for review
     */
    public List<Question> getDueQuestions(List<Question> allQuestions) {
        long now = System.currentTimeMillis();
        List<Question> due = new ArrayList<>();

        for (Question q : allQuestions) {
            if (q.getDifficulty() > currentLevel) continue;

            long nextReview = nextReviewCache.getOrDefault(q.getId(), 0L);
            if (nextReview <= now) {
                due.add(q);
            }
        }

        // Sort by priority: overdue first, then by interval
        Collections.sort(due, (q1, q2) -> {
            long t1 = nextReviewCache.getOrDefault(q1.getId(), 0L);
            long t2 = nextReviewCache.getOrDefault(q2.getId(), 0L);
            return Long.compare(t1, t2);
        });

        return due;
    }

    /**
     * Get next questions for study session
     */
    public List<Question> getNextQuestions(List<Question> allQuestions, int count) {
        List<Question> due = getDueQuestions(allQuestions);

        // Auto-level up if current level mastered
        if (due.isEmpty()) {
            int maxDifficulty = getMaxDifficulty(allQuestions);
            if (currentLevel < maxDifficulty) {
                currentLevel++;
                prefs.edit().putInt(KEY_LEVEL, currentLevel).apply();
                return getNextQuestions(allQuestions, count);
            }
        }

        return due.subList(0, Math.min(count, due.size()));
    }

    private int getMaxDifficulty(List<Question> questions) {
        int max = 1;
        for (Question q : questions) {
            if (q.getDifficulty() > max) max = q.getDifficulty();
        }
        return max;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setLevel(int level) {
        this.currentLevel = level;
        prefs.edit().putInt(KEY_LEVEL, level).apply();
    }

    /**
     * Get statistics
     */
    public Stats getStats(List<Question> allQuestions) {
        int total = 0;
        int mastered = 0;
        int learning = 0;
        int young = 0;

        for (Question q : allQuestions) {
            if (q.getDifficulty() <= currentLevel) {
                total++;
                long interval = intervalCache.getOrDefault(q.getId(), INTERVAL_NEW);

                if (interval >= INTERVAL_MATURE_BASE) {
                    mastered++;
                } else if (interval >= INTERVAL_YOUNG) {
                    young++;
                } else if (interval > INTERVAL_NEW) {
                    learning++;
                }
            }
        }

        return new Stats(total, mastered, learning, young);
    }

    public static class Stats {
        public final int total;
        public final int mastered;
        public final int learning;
        public final int young;

        public Stats(int total, int mastered, int learning, int young) {
            this.total = total;
            this.mastered = mastered;
            this.learning = learning;
            this.young = young;
        }

        public int getMasteryPercent() {
            return total > 0 ? (mastered * 100 / total) : 0;
        }
    }
}
