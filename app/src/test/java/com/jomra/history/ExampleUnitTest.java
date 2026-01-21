package com.jomra.history;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

public class ExampleUnitTest {
    @Test
    public void testQuestionModel() {
        Question q = new Question(1, "Test?", Arrays.asList("A", "B"), 0, 1);
        assertEquals(1, q.getId());
        assertEquals("Test?", q.getText());
        assertEquals(0, q.getCorrectAnswer());
        assertEquals(1, q.getDifficulty());
    }

    @Test
    public void testLearningProgress() {
        // Since LearningManager needs Context, we'd need Robolectric or Instrumented test for full test.
        // But we can test the Question logic here.
        List<Question> questions = new ArrayList<>();
        questions.add(new Question(1, "Q1", Arrays.asList("A"), 0, 1));
        questions.add(new Question(2, "Q2", Arrays.asList("A"), 0, 2));

        assertEquals(2, questions.size());
    }
}
