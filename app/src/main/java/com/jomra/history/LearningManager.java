package com.jomra.history;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LearningManager {
    private static final String PREFS_NAME = "JomraProgress";
    private static final String KEY_MASTERED = "mastered_questions";
    private static final String KEY_LEVEL = "current_level";

    private SharedPreferences prefs;
    private Set<String> masteredIds;
    private int currentLevel;

    public LearningManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        masteredIds = new HashSet<>(prefs.getStringSet(KEY_MASTERED, new HashSet<>()));
        currentLevel = prefs.getInt(KEY_LEVEL, 1);
    }

    public void markMastered(int questionId) {
        masteredIds.add(String.valueOf(questionId));
        save();
    }

    public boolean isMastered(int questionId) {
        return masteredIds.contains(String.valueOf(questionId));
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setLevel(int level) {
        currentLevel = level;
        save();
    }

    private void save() {
        prefs.edit()
            .putStringSet(KEY_MASTERED, masteredIds)
            .putInt(KEY_LEVEL, currentLevel)
            .apply();
    }

    public List<Question> getNextQuestions(List<Question> allQuestions, int count) {
        List<Question> available = new ArrayList<>();
        int maxDifficulty = 0;
        for (Question q : allQuestions) {
            if (q.getDifficulty() > maxDifficulty) maxDifficulty = q.getDifficulty();
            // Show questions of current level or lower if not mastered
            if (q.getDifficulty() <= currentLevel && !isMastered(q.getId())) {
                available.add(q);
            }
        }

        // If not enough questions in current/lower levels, include some mastered ones for review or move up level if empty
        if (available.size() < count) {
            // Check if we should increase level
            boolean allLowerMastered = true;
            for (Question q : allQuestions) {
                if (q.getDifficulty() <= currentLevel && !isMastered(q.getId())) {
                    allLowerMastered = false;
                    break;
                }
            }
            if (allLowerMastered && currentLevel < maxDifficulty) {
                currentLevel++;
                save();
                return getNextQuestions(allQuestions, count); // Retry with new level
            }
        }

        Collections.shuffle(available);
        return available.subList(0, Math.min(count, available.size()));
    }
}
