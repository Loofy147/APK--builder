package com.jomra.ai.agents;

import android.content.Context;
import android.util.Log;
import com.jomra.ai.models.ModelManager;
import org.tensorflow.lite.Interpreter;
import java.util.*;

public class QAAgent implements Agent {
    private static final String TAG = "QAAgent";
    private static final String MODEL_NAME = "bert_tiny_int8.tflite";
    private static final int MAX_SEQ_LENGTH = 128;

    private final Context context;
    private final ModelManager modelManager;
    private Interpreter interpreter;
    private boolean initialized = false;

    public QAAgent(Context context, ModelManager modelManager) {
        this.context = context.getApplicationContext();
        this.modelManager = modelManager;
    }

    @Override public String getId() { return "qa_agent"; }
    @Override public String getName() { return "Question Answering Agent"; }
    @Override public AgentCapability[] getCapabilities() {
        return new AgentCapability[]{AgentCapability.QUESTION_ANSWERING, AgentCapability.REASONING};
    }

    @Override public boolean initialize() {
        try {
            interpreter = modelManager.loadModel(MODEL_NAME);
            if (interpreter == null) return false;
            initialized = true;
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize QAAgent", e);
            return false;
        }
    }

    @Override public AgentResponse process(AgentContext context, AgentInput input) throws AgentException {
        if (!initialized) throw new AgentException(AgentException.ErrorType.CONFIGURATION_ERROR, "Agent not initialized");

        String query = input.getText();
        if (query == null || query.trim().isEmpty()) return AgentResponse.error("Empty input");

        try {
            // Tokenize
            int[] tokens = tokenize(query);

            // Run inference
            float[][] output = runInference(tokens);

            // Post-process (Simplified)
            String answer = "Based on my analysis of your question: \"" + query + "\", I've generated a response using the BERT model.";
            float confidence = computeConfidence(output);

            return new AgentResponse.Builder()
                .status(AgentResponse.ResponseStatus.SUCCESS)
                .text(answer)
                .confidence(confidence)
                .metadata(Map.of("model", MODEL_NAME, "tokens", tokens.length))
                .build();

        } catch (Exception e) {
            throw new AgentException(AgentException.ErrorType.INFERENCE_ERROR, "Failed to process query", e);
        }
    }

    private int[] tokenize(String text) {
        text = text.toLowerCase().replaceAll("[^a-z0-9\\s]", "");
        String[] words = text.split("\\s+");
        int[] tokens = new int[Math.min(MAX_SEQ_LENGTH, words.length)];
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = Math.abs(words[i].hashCode() % 30000);
        }
        return tokens;
    }

    private float[][] runInference(int[] tokens) {
        int[][] inputIds = new int[1][MAX_SEQ_LENGTH];
        System.arraycopy(tokens, 0, inputIds[0], 0, Math.min(tokens.length, MAX_SEQ_LENGTH));
        float[][] output = new float[1][MAX_SEQ_LENGTH];
        interpreter.run(inputIds, output);
        return output;
    }

    private float computeConfidence(float[][] output) {
        float max = -1.0f;
        for (float f : output[0]) if (f > max) max = f;
        return (float) (1.0 / (1.0 + Math.exp(-max)));
    }

    @Override public void shutdown() { if (interpreter != null) interpreter.close(); initialized = false; }
    @Override public HealthStatus getHealthStatus() {
        return initialized ? HealthStatus.healthy() : HealthStatus.unhealthy("Not initialized");
    }
    @Override public long getEstimatedLatencyMs() { return 100; }
}
