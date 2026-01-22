package com.jomra.history;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<Question> allQuestions = new ArrayList<>();
    private LearningManager learningManager;
    private Question currentQuestion;
    private int sessionCorrectCount = 0;
    private int sessionTotalCount = 0;

    private TextView tvLevel, tvProgress, tvQuestion, tvSessionStats, tvDueCount;
    private ProgressBar progressBar;
    private Button btn1, btn2, btn3, btn4, btnDontKnow;
    private View loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        learningManager = new LearningManager(this);
        loadQuestions();
        showNextQuestion();
    }

    private void initViews() {
        tvLevel = findViewById(R.id.tvLevel);
        tvProgress = findViewById(R.id.tvProgress);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvSessionStats = findViewById(R.id.tvSessionStats);
        tvDueCount = findViewById(R.id.tvDueCount);
        progressBar = findViewById(R.id.progressBar);
        loadingView = findViewById(R.id.loadingView);

        btn1 = findViewById(R.id.btnOption1);
        btn2 = findViewById(R.id.btnOption2);
        btn3 = findViewById(R.id.btnOption3);
        btn4 = findViewById(R.id.btnOption4);
        btnDontKnow = findViewById(R.id.btnDontKnow);

        btn1.setOnClickListener(v -> checkAnswer(0, btn1));
        btn2.setOnClickListener(v -> checkAnswer(1, btn2));
        btn3.setOnClickListener(v -> checkAnswer(2, btn3));
        btn4.setOnClickListener(v -> checkAnswer(3, btn4));
        btnDontKnow.setOnClickListener(v -> checkAnswer(-1, btnDontKnow));
    }

    private void loadQuestions() {
        try {
            showLoading(true);

            InputStream is = getAssets().open("questions.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);

            // Use Gson for efficient parsing
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Question>>(){}.getType();
            allQuestions = gson.fromJson(json, listType);

            showLoading(false);
        } catch (Exception e) {
            e.printStackTrace();
            showLoading(false);
            Toast.makeText(this, "Error loading questions: " + e.getMessage(),
                Toast.LENGTH_LONG).show();
        }
    }

    private void showNextQuestion() {
        List<Question> next = learningManager.getNextQuestions(allQuestions, 1);

        if (next.isEmpty()) {
            showCompletionState();
            return;
        }

        currentQuestion = next.get(0);
        displayQuestion();
        updateStatus();
        resetButtonStates();
    }

    private void displayQuestion() {
        // Fade in animation
        tvQuestion.setAlpha(0f);
        tvQuestion.setText(currentQuestion.getText());
        tvQuestion.animate().alpha(1f).setDuration(300).start();

        List<String> options = currentQuestion.getOptions();
        btn1.setText(options.get(0));
        btn2.setText(options.get(1));
        btn3.setText(options.get(2));
        btn4.setText(options.get(3));
    }

    private void checkAnswer(int selectedIndex, Button selectedButton) {
        sessionTotalCount++;
        boolean correct = selectedIndex == currentQuestion.getCorrectAnswer();

        // Record answer in SRS system
        if (selectedIndex != -1) {
            learningManager.recordAnswer(currentQuestion.getId(), correct);
        }

        // Visual feedback
        if (correct) {
            sessionCorrectCount++;
            showCorrectFeedback(selectedButton);
        } else {
            showIncorrectFeedback(selectedButton);
        }

        // Delay before next question
        selectedButton.postDelayed(this::showNextQuestion, 800);
    }

    private void showCorrectFeedback(Button button) {
        button.setBackgroundColor(Color.parseColor("#4CAF50"));
        animateButton(button);
        Toast.makeText(this, "✓ Correct!", Toast.LENGTH_SHORT).show();
    }

    private void showIncorrectFeedback(Button button) {
        button.setBackgroundColor(Color.parseColor("#F44336"));
        animateButton(button);

        String correctAnswer = currentQuestion.getOptions()
            .get(currentQuestion.getCorrectAnswer());
        Toast.makeText(this, "✗ Correct answer: " + correctAnswer,
            Toast.LENGTH_LONG).show();
    }

    private void animateButton(Button button) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f, 1f);
        scaleX.setDuration(200);
        scaleY.setDuration(200);
        scaleX.start();
        scaleY.start();
    }

    private void resetButtonStates() {
        int defaultColor = getResources().getColor(android.R.color.holo_blue_light);
        btn1.setBackgroundColor(defaultColor);
        btn2.setBackgroundColor(defaultColor);
        btn3.setBackgroundColor(defaultColor);
        btn4.setBackgroundColor(defaultColor);

        btn1.setEnabled(true);
        btn2.setEnabled(true);
        btn3.setEnabled(true);
        btn4.setEnabled(true);
        btnDontKnow.setEnabled(true);
    }

    private void updateStatus() {
        tvLevel.setText("Level " + learningManager.getCurrentLevel());

        LearningManager.Stats stats = learningManager.getStats(allQuestions);
        progressBar.setMax(stats.total);
        progressBar.setProgress(stats.mastered);

        tvProgress.setText(String.format("Mastery: %d%% (%d/%d)",
            stats.getMasteryPercent(), stats.mastered, stats.total));

        if (sessionTotalCount > 0) {
            int sessionPercent = (sessionCorrectCount * 100) / sessionTotalCount;
            tvSessionStats.setText(String.format("Session: %d/%d (%d%%)",
                sessionCorrectCount, sessionTotalCount, sessionPercent));
            tvSessionStats.setVisibility(View.VISIBLE);
        }

        List<Question> dueQuestions = learningManager.getDueQuestions(allQuestions);
        tvDueCount.setText(String.format("%d cards due", dueQuestions.size()));
    }

    private void showCompletionState() {
        tvQuestion.setText("🎉 Congratulations!\n\nYou've reviewed all due cards.");
        btn1.setEnabled(false);
        btn2.setEnabled(false);
        btn3.setEnabled(false);
        btn4.setEnabled(false);
        btnDontKnow.setEnabled(false);

        LearningManager.Stats stats = learningManager.getStats(allQuestions);
        if (stats.getMasteryPercent() == 100) {
            tvQuestion.append("\n\nYou've mastered all questions at this level!");
        }
    }

    private void showLoading(boolean show) {
        loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
        tvQuestion.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
