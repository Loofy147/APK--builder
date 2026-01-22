package com.jomra.ai;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.jomra.ai.agents.*;
import com.jomra.ai.api.APIClient;
import com.jomra.ai.models.ModelManager;
import com.jomra.ai.tools.ToolRegistry;
import java.util.Calendar;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TextInputEditText etUserInput;
    private MaterialButton btnSend, btnToolMode, btnRLMode, btnClearHistory;
    private TextView tvResponse, tvAgentInfo, tvStats;
    private ProgressBar progressBar;
    private ScrollView scrollView;
    private LinearLayout llHistory;

    private AgentOrchestrator orchestrator;
    private ModelManager modelManager;
    private ToolRegistry toolRegistry;
    private APIClient apiClient;
    private ConversationHistory conversationHistory;
    private AppState currentAppState;

    private AgentMode currentMode = AgentMode.QA;
    private int messageCount = 0;

    private enum AgentMode { QA, TOOL, RL }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
        initializeAISystem();
    }

    private void initializeViews() {
        etUserInput = findViewById(R.id.etUserInput);
        btnSend = findViewById(R.id.btnSend);
        btnToolMode = findViewById(R.id.btnToolMode);
        btnRLMode = findViewById(R.id.btnRLMode);
        btnClearHistory = findViewById(R.id.btnClearHistory);
        tvResponse = findViewById(R.id.tvResponse);
        tvAgentInfo = findViewById(R.id.tvAgentInfo);
        tvStats = findViewById(R.id.tvStats);
        progressBar = findViewById(R.id.progressBar);
        scrollView = findViewById(R.id.scrollView);
        llHistory = findViewById(R.id.llHistory);

        btnSend.setOnClickListener(v -> handleUserInput());
        btnToolMode.setOnClickListener(v -> switchMode(AgentMode.TOOL));
        btnRLMode.setOnClickListener(v -> switchMode(AgentMode.RL));
        btnClearHistory.setOnClickListener(v -> clearHistory());
    }

    private void initializeAISystem() {
        showLoading(true);
        tvResponse.setText("Initializing AI agents...");
        new Thread(() -> {
            try {
                modelManager = new ModelManager(this);
                toolRegistry = new ToolRegistry(this);
                apiClient = new APIClient();
                orchestrator = new AgentOrchestrator();
                orchestrator.registerAgent(new QAAgent(this, modelManager));
                orchestrator.registerAgent(new RLAgent(this, modelManager));
                orchestrator.registerAgent(new ToolAgent(this, toolRegistry));
                conversationHistory = new ConversationHistory(20);
                currentAppState = buildAppState();
                runOnUiThread(() -> {
                    showLoading(false);
                    tvResponse.setText("AI system ready!");
                    updateAgentInfo();
                    updateStats();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    tvResponse.setText("Error: " + e.getMessage());
                });
            }
        }).start();
    }

    private void handleUserInput() {
        String input = etUserInput.getText().toString().trim();
        if (input.isEmpty()) return;
        addMessageToHistory("You", input, true);
        etUserInput.setText("");
        showLoading(true);
        new Thread(() -> {
            try {
                AgentContext context = new AgentContext.Builder()
                    .appState(currentAppState)
                    .tools(toolRegistry)
                    .apiClient(apiClient)
                    .history(conversationHistory)
                    .training(currentMode == AgentMode.RL)
                    .build();
                AgentResponse response = orchestrator.processSingle(context, input);
                if (response != null && response.isSuccess()) {
                    conversationHistory.addTurn(input, response.getText());
                }
                runOnUiThread(() -> {
                    showLoading(false);
                    displayResponse(response);
                    updateStats();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    displayError("Error: " + e.getMessage());
                });
            }
        }).start();
    }

    private void displayResponse(AgentResponse response) {
        if (response == null) return;
        String text = response.getText();
        if (response.getConfidence() > 0) text += String.format("\n\nConf: %.1f%%", response.getConfidence() * 100);
        if (response.getSuggestedAction() != null) text += "\n\nAction: " + response.getSuggestedAction().getDescription();
        addMessageToHistory("Agent", text, false);
        tvResponse.setText(text);
        messageCount++;
    }

    private void displayError(String error) {
        addMessageToHistory("System", error, false);
        tvResponse.setText(error);
    }

    private void addMessageToHistory(String sender, String message, boolean isUser) {
        View view = getLayoutInflater().inflate(R.layout.item_message, llHistory, false);
        ((TextView)view.findViewById(R.id.tvSender)).setText(sender);
        ((TextView)view.findViewById(R.id.tvMessage)).setText(message);
        if (isUser) view.setBackgroundColor(0xFFE3F2FD);
        llHistory.addView(view);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void switchMode(AgentMode mode) {
        currentMode = mode;
        updateAgentInfo();
        btnToolMode.setEnabled(mode != AgentMode.TOOL);
        btnRLMode.setEnabled(mode != AgentMode.RL);
    }

    private void updateAgentInfo() {
        String info = "Mode: " + currentMode + "\nAgents: " + orchestrator.getAgentHealth().size();
        tvAgentInfo.setText(info);
    }

    private void updateStats() {
        tvStats.setText("Msgs: " + messageCount + " | Turns: " + conversationHistory.getTurns().size());
    }

    private void clearHistory() {
        llHistory.removeAllViews();
        conversationHistory = new ConversationHistory(20);
        messageCount = 0;
        updateStats();
    }

    private AppState buildAppState() {
        Calendar cal = Calendar.getInstance();
        return new AppState.Builder().hourOfDay(cal.get(Calendar.HOUR_OF_DAY))
            .dayOfWeek(cal.get(Calendar.DAY_OF_WEEK)).batteryLevel(100f).build();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSend.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orchestrator != null) orchestrator.shutdown();
        if (modelManager != null) modelManager.unloadAll();
    }
}
