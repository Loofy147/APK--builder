package com.jomra.ai;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import android.content.Intent;
import com.jomra.ai.agents.*;
import com.jomra.ai.agents.advanced.*;
import com.jomra.ai.api.APIClient;
import com.jomra.ai.memory.MemorySystem;
import com.jomra.ai.models.ModelCatalog;
import com.jomra.ai.models.ModelInfo;
import com.jomra.ai.models.ModelManager;
import com.jomra.ai.tools.ToolRegistry;
import java.util.Calendar;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TextInputEditText etUserInput;
    private MaterialButton btnSend;
    private TextView tvResponse, tvAgentInfo, tvStats;
    private ProgressBar progressBar;
    private ScrollView scrollView;
    private LinearLayout llHistory;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private AgentOrchestrator orchestrator;
    private ModelManager modelManager;
    private ToolRegistry toolRegistry;
    private APIClient apiClient;
    private MemorySystem memorySystem;
    private ConversationHistory conversationHistory;
    private AppState currentAppState;

    private AgentMode currentMode = AgentMode.QA;
    private int messageCount = 0;

    private enum AgentMode { QA, TOOL, RL, MOAZIZ }

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
        tvResponse = findViewById(R.id.tvResponse);
        tvAgentInfo = findViewById(R.id.tvAgentInfo);
        tvStats = findViewById(R.id.tvStats);
        progressBar = findViewById(R.id.progressBar);
        scrollView = findViewById(R.id.scrollView);
        llHistory = findViewById(R.id.llHistory);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        if (navigationView == null) {
            Log.e(TAG, "NavigationView not found in layout!");
        }

        btnSend.setOnClickListener(v -> handleUserInput());

        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_marketplace) {
                startActivity(new Intent(this, com.jomra.ai.ui.ModelMarketplaceActivity.class));
            } else if (id == R.id.nav_clear_history) {
                clearHistory();
            } else if (id == R.id.mode_qa) {
                switchMode(AgentMode.QA);
            } else if (id == R.id.mode_tool) {
                switchMode(AgentMode.TOOL);
            } else if (id == R.id.mode_rl) {
                switchMode(AgentMode.RL);
            } else if (id == R.id.mode_moaziz) {
                switchMode(AgentMode.MOAZIZ);
            } else if (id == R.id.nav_settings) {
                Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void initializeAISystem() {
        showLoading(true);
        tvResponse.setText("Initializing AI agents...");
        new Thread(() -> {
            try {
                // Check for models in assets
                boolean hasModels = false;
                try {
                    String[] models = getAssets().list("models");
                    hasModels = models != null && models.length > 0;
                } catch (Exception e) {
                    Log.w(TAG, "No models directory found in assets", e);
                }

                modelManager = new ModelManager(this);
                toolRegistry = new ToolRegistry(this);
                apiClient = new APIClient();
                memorySystem = new MemorySystem(this);
                orchestrator = new AgentOrchestrator();

                Agent qaAgent = new QAAgent(this, modelManager);
                orchestrator.registerAgent(qaAgent);
                orchestrator.registerAgent(new RLAgent(this, modelManager));
                orchestrator.registerAgent(new ToolAgent(this, toolRegistry));

                // Advanced Agents
                orchestrator.registerAgent(new ChainOfThoughtAgent(this, modelManager, toolRegistry, qaAgent, memorySystem));
                orchestrator.registerAgent(new MultimodalAgent(this, modelManager));
                orchestrator.registerAgent(new PlanningAgent());

                MoazizAgent moazizAgent = new MoazizAgent(this, memorySystem);
                moazizAgent.registerInternalAgent(qaAgent);
                moazizAgent.registerInternalAgent(new ToolAgent(this, toolRegistry));
                moazizAgent.registerInternalAgent(new ChainOfThoughtAgent(this, modelManager, toolRegistry, qaAgent, memorySystem));
                orchestrator.registerAgent(moazizAgent);

                // Dynamically register agents for downloaded models
                ModelCatalog catalog = new ModelCatalog(this);
                for (ModelInfo model : catalog.getDownloadedModels()) {
                    if (model.id.contains("mistral")) {
                        orchestrator.registerAgent(new QAAgent(this, modelManager)); // Use QAAgent as placeholder for specialized model loader
                    }
                }

                conversationHistory = new ConversationHistory(20);
                currentAppState = buildAppState();

                final boolean finalHasModels = hasModels;
                runOnUiThread(() -> {
                    showLoading(false);
                    if (!finalHasModels) {
                        tvResponse.setText("AI system ready (No models found, using fallback logic)");
                    } else {
                        tvResponse.setText("AI system ready!");
                    }
                    updateAgentInfo();
                    updateStats();
                });
            } catch (Exception e) {
                Log.e(TAG, "Initialization failed", e);
                runOnUiThread(() -> {
                    showLoading(false);
                    tvResponse.setText("Initialization failed: " + e.getMessage());
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
                AgentResponse response;
                if (currentMode == AgentMode.MOAZIZ) {
                    response = orchestrator.processPipeline(context, input, new String[]{"moaziz_agent"});
                } else {
                    response = orchestrator.processSingle(context, input);
                }

                if (response != null && response.isSuccess()) {
                    conversationHistory.addTurn(input, response.getText());
                    memorySystem.remember(input, response.getText(), 0.5f, null);
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
        int layoutRes = isUser ? R.layout.item_message_user : R.layout.item_message_agent;
        View view = getLayoutInflater().inflate(layoutRes, llHistory, false);

        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(message);

        if (!isUser) {
            TextView tvSender = view.findViewById(R.id.tvSender);
            if (tvSender != null) tvSender.setText(sender);
        }

        llHistory.addView(view);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void switchMode(AgentMode mode) {
        currentMode = mode;
        updateAgentInfo();
        // Update navigation menu check state
        int menuId = R.id.mode_qa;
        if (mode == AgentMode.TOOL) menuId = R.id.mode_tool;
        else if (mode == AgentMode.RL) menuId = R.id.mode_rl;
        else if (mode == AgentMode.MOAZIZ) menuId = R.id.mode_moaziz;
        navigationView.setCheckedItem(menuId);
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

    private View typingIndicator;

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSend.setEnabled(!show);

        if (show) {
            typingIndicator = getLayoutInflater().inflate(R.layout.item_message_agent, llHistory, false);
            TextView tvMsg = typingIndicator.findViewById(R.id.tvMessage);
            tvMsg.setText("...");
            llHistory.addView(typingIndicator);
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        } else if (typingIndicator != null) {
            llHistory.removeView(typingIndicator);
            typingIndicator = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orchestrator != null) orchestrator.shutdown();
        if (modelManager != null) modelManager.unloadAll();
    }
}
