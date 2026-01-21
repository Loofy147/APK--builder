package com.jomra.history;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<Question> allQuestions = new ArrayList<>();
    private LearningManager learningManager;
    private Question currentQuestion;
    private int sessionCorrectCount = 0;
    private int sessionTotalCount = 0;

    private TextView tvLevel, tvProgress, tvQuestion;
    private Button btn1, btn2, btn3, btn4, btnDontKnow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLevel = findViewById(R.id.tvLevel);
        tvProgress = findViewById(R.id.tvProgress);
        tvQuestion = findViewById(R.id.tvQuestion);
        btn1 = findViewById(R.id.btnOption1);
        btn2 = findViewById(R.id.btnOption2);
        btn3 = findViewById(R.id.btnOption3);
        btn4 = findViewById(R.id.btnOption4);
        btnDontKnow = findViewById(R.id.btnDontKnow);

        learningManager = new LearningManager(this);
        loadQuestions();
        showNextQuestion();

        btn1.setOnClickListener(v -> checkAnswer(0));
        btn2.setOnClickListener(v -> checkAnswer(1));
        btn3.setOnClickListener(v -> checkAnswer(2));
        btn4.setOnClickListener(v -> checkAnswer(3));
        btnDontKnow.setOnClickListener(v -> checkAnswer(-1));
    }

    private void loadQuestions() {
        try {
            InputStream is = getAssets().open("questions.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                JSONArray optionsArray = obj.getJSONArray("options");
                List<String> options = new ArrayList<>();
                for (int j = 0; j < optionsArray.length(); j++) {
                    options.add(optionsArray.getString(j));
                }
                allQuestions.add(new Question(
                    obj.getInt("id"),
                    obj.getString("text"),
                    options,
                    obj.getInt("correctAnswer"),
                    obj.getInt("difficulty")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading questions", Toast.LENGTH_SHORT).show();
        }
    }

    private void showNextQuestion() {
        List<Question> next = learningManager.getNextQuestions(allQuestions, 1);
        if (next.isEmpty()) {
            tvQuestion.setText("Congratulations! You've mastered all current history questions.");
            btn1.setEnabled(false);
            btn2.setEnabled(false);
            btn3.setEnabled(false);
            btn4.setEnabled(false);
            btnDontKnow.setEnabled(false);
            return;
        }

        currentQuestion = next.get(0);
        tvQuestion.setText(currentQuestion.getText());
        List<String> options = currentQuestion.getOptions();
        btn1.setText(options.get(0));
        btn2.setText(options.get(1));
        btn3.setText(options.get(2));
        btn4.setText(options.get(3));

        updateStatus();
    }

    private void checkAnswer(int selectedIndex) {
        sessionTotalCount++;
        if (selectedIndex == currentQuestion.getCorrectAnswer()) {
            sessionCorrectCount++;
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            learningManager.markMastered(currentQuestion.getId());
        } else if (selectedIndex == -1) {
            Toast.makeText(this, "The correct answer was: " + currentQuestion.getOptions().get(currentQuestion.getCorrectAnswer()), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Incorrect. Try again later!", Toast.LENGTH_SHORT).show();
        }
        showNextQuestion();
    }

    private void updateStatus() {
        tvLevel.setText("Level: " + learningManager.getCurrentLevel());
        int total = allQuestions.size();
        int mastered = 0;
        for (Question q : allQuestions) {
            if (learningManager.isMastered(q.getId())) mastered++;
        }
        int percent = (total > 0) ? (mastered * 100 / total) : 0;
        tvProgress.setText("Mastery: " + percent + "%");
    }
}
