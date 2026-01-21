package com.jomra.history;

import java.util.List;

public class Question {
    private int id;
    private String text;
    private List<String> options;
    private int correctAnswer;
    private int difficulty;

    public Question(int id, String text, List<String> options, int correctAnswer, int difficulty) {
        this.id = id;
        this.text = text;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.difficulty = difficulty;
    }

    public int getId() { return id; }
    public String getText() { return text; }
    public List<String> getOptions() { return options; }
    public int getCorrectAnswer() { return correctAnswer; }
    public int getDifficulty() { return difficulty; }
}
