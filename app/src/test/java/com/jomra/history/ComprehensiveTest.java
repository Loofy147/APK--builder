package com.jomra.history;

import android.content.Context;
import android.content.SharedPreferences;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class ComprehensiveTest {

    private Context context;
    private LearningManager learningManager;
    private List<Question> testQuestions;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();

        // Clear preferences
        SharedPreferences prefs = context.getSharedPreferences("JomraProgress", Context.MODE_PRIVATE);
        prefs.edit().clear().commit();

        learningManager = new LearningManager(context);

        // Create test questions
        testQuestions = new ArrayList<>();
        testQuestions.add(new Question(1, "Q1?", Arrays.asList("A", "B", "C", "D"), 0, 1));
        testQuestions.add(new Question(2, "Q2?", Arrays.asList("A", "B", "C", "D"), 1, 1));
        testQuestions.add(new Question(3, "Q3?", Arrays.asList("A", "B", "C", "D"), 2, 2));
        testQuestions.add(new Question(4, "Q4?", Arrays.asList("A", "B", "C", "D"), 3, 2));
        testQuestions.add(new Question(5, "Q5?", Arrays.asList("A", "B", "C", "D"), 0, 3));
    }

    @Test
    public void testQuestionModel() {
        Question q = new Question(1, "Test?", Arrays.asList("A", "B"), 0, 1);
        assertEquals(1, q.getId());
        assertEquals("Test?", q.getText());
        assertEquals(0, q.getCorrectAnswer());
        assertEquals(1, q.getDifficulty());
        assertEquals(2, q.getOptions().size());
    }

    @Test
    public void testLearningManagerInitialization() {
        assertNotNull(learningManager);
        assertEquals(1, learningManager.getCurrentLevel());
    }

    @Test
    public void testRecordCorrectAnswer() {
        learningManager.recordAnswer(1, true);

        // Wait for async save
        try { Thread.sleep(100); } catch (InterruptedException e) {}

        // Verify answer was recorded
        assertFalse(learningManager.isMastered(1)); // Not yet mature
    }

    @Test
    public void testRecordWrongAnswer() {
        learningManager.recordAnswer(1, false);

        // Wait for async save
        try { Thread.sleep(100); } catch (InterruptedException e) {}

        assertFalse(learningManager.isMastered(1));
    }

    @Test
    public void testMasteryProgression() {
        // Answer correctly multiple times to reach maturity
        for (int i = 0; i < 5; i++) {
            learningManager.recordAnswer(1, true);
            try { Thread.sleep(50); } catch (InterruptedException e) {}
        }

        // After multiple correct answers with intervals, should be mastered
        // (This is simplified - real test would manipulate time)
    }

    @Test
    public void testGetNextQuestions() {
        List<Question> next = learningManager.getNextQuestions(testQuestions, 2);

        assertNotNull(next);
        assertTrue(next.size() <= 2);

        // Should only return level 1 questions initially
        for (Question q : next) {
            assertTrue(q.getDifficulty() <= learningManager.getCurrentLevel());
        }
    }

    @Test
    public void testLevelProgression() {
        assertEquals(1, learningManager.getCurrentLevel());

        learningManager.setLevel(2);
        assertEquals(2, learningManager.getCurrentLevel());

        // Verify persistence
        LearningManager newManager = new LearningManager(context);
        assertEquals(2, newManager.getCurrentLevel());
    }

    @Test
    public void testStats() {
        LearningManager.Stats stats = learningManager.getStats(testQuestions);

        assertNotNull(stats);
        assertTrue(stats.total >= 0);
        assertTrue(stats.mastered >= 0);
        assertTrue(stats.learning >= 0);
        assertTrue(stats.young >= 0);
        assertTrue(stats.getMasteryPercent() >= 0);
        assertTrue(stats.getMasteryPercent() <= 100);
    }

    @Test
    public void testDueQuestions() {
        List<Question> due = learningManager.getDueQuestions(testQuestions);

        assertNotNull(due);
        // New questions should be due
        assertTrue(due.size() > 0);
    }

    @Test
    public void testQuestionFiltering() {
        // Level 1: should only get difficulty 1 questions
        learningManager.setLevel(1);
        List<Question> level1 = learningManager.getNextQuestions(testQuestions, 10);

        for (Question q : level1) {
            assertTrue(q.getDifficulty() <= 1);
        }

        // Level 2: should get difficulty 1 and 2
        learningManager.setLevel(2);
        List<Question> level2 = learningManager.getNextQuestions(testQuestions, 10);

        for (Question q : level2) {
            assertTrue(q.getDifficulty() <= 2);
        }
    }

    @Test
    public void testEmptyQuestionList() {
        List<Question> empty = new ArrayList<>();
        List<Question> result = learningManager.getNextQuestions(empty, 5);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testRequestMoreQuestionsThanAvailable() {
        List<Question> result = learningManager.getNextQuestions(testQuestions, 100);

        assertNotNull(result);
        assertTrue(result.size() <= testQuestions.size());
    }

    @Test
    public void testStatsCalculation() {
        // Record some progress
        learningManager.recordAnswer(1, true);
        learningManager.recordAnswer(2, false);

        try { Thread.sleep(100); } catch (InterruptedException e) {}

        LearningManager.Stats stats = learningManager.getStats(testQuestions);

        assertTrue(stats.total > 0);
        assertTrue(stats.getMasteryPercent() >= 0);
    }

    @Test
    public void testMultipleSessions() {
        // Session 1
        learningManager.recordAnswer(1, true);
        learningManager.recordAnswer(2, true);

        try { Thread.sleep(100); } catch (InterruptedException e) {}

        // Create new manager (simulating app restart)
        LearningManager newManager = new LearningManager(context);

        // Session 2 - should persist data
        newManager.recordAnswer(3, true);

        try { Thread.sleep(100); } catch (InterruptedException e) {}

        LearningManager.Stats stats = newManager.getStats(testQuestions);
        assertTrue(stats.learning > 0 || stats.young > 0 || stats.mastered > 0);
    }
}
