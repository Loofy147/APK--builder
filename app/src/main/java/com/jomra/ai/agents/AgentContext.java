package com.jomra.ai.agents;

import com.jomra.ai.api.APIClient;
import com.jomra.ai.tools.ToolRegistry;
import java.util.Collections;
import java.util.Map;

public class AgentContext {
    private final AppState appState;
    private final ToolRegistry toolRegistry;
    private final APIClient apiClient;
    private final ConversationHistory history;
    private final Map<String, Object> metadata;
    private final boolean trainingMode;

    private AgentContext(Builder builder) {
        this.appState = builder.appState;
        this.toolRegistry = builder.toolRegistry;
        this.apiClient = builder.apiClient;
        this.history = builder.history;
        this.metadata = builder.metadata;
        this.trainingMode = builder.trainingMode;
    }

    public AppState getAppState() { return appState; }
    public ToolRegistry getToolRegistry() { return toolRegistry; }
    public APIClient getApiClient() { return apiClient; }
    public ConversationHistory getHistory() { return history; }
    public Map<String, Object> getMetadata() { return metadata; }
    public boolean isTraining() { return trainingMode; }

    public static class Builder {
        private AppState appState;
        private ToolRegistry toolRegistry;
        private APIClient apiClient;
        private ConversationHistory history;
        private Map<String, Object> metadata = Collections.emptyMap();
        private boolean trainingMode = false;

        public Builder appState(AppState state) {
            this.appState = state;
            return this;
        }

        public Builder tools(ToolRegistry registry) {
            this.toolRegistry = registry;
            return this;
        }

        public Builder apiClient(APIClient client) {
            this.apiClient = client;
            return this;
        }

        public Builder history(ConversationHistory hist) {
            this.history = hist;
            return this;
        }

        public Builder metadata(Map<String, Object> meta) {
            this.metadata = meta;
            return this;
        }

        public Builder training(boolean training) {
            this.trainingMode = training;
            return this;
        }

        public AgentContext build() {
            return new AgentContext(this);
        }
    }
}
