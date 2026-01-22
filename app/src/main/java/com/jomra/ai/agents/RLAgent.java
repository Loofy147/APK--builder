package com.jomra.ai.agents;

import android.content.Context;
import android.util.Log;
import com.jomra.ai.models.ModelManager;
import com.jomra.ai.rl.*;
import org.tensorflow.lite.Interpreter;
import java.util.*;

public class RLAgent implements Agent {
    private static final String TAG = "RLAgent";
    private static final String MODEL_NAME = "dqn_fp16.tflite";
    private static final int STATE_DIM = 50;
    private static final int ACTION_SPACE_SIZE = 10;

    private final Context context;
    private final ModelManager modelManager;
    private final StateEncoder stateEncoder;
    private final ReplayBuffer replayBuffer;
    private Interpreter interpreter;
    private boolean initialized = false;
    private float[] lastState;
    private int lastAction;

    public RLAgent(Context context, ModelManager modelManager) {
        this.context = context.getApplicationContext();
        this.modelManager = modelManager;
        this.stateEncoder = new StateEncoder();
        this.replayBuffer = new ReplayBuffer(1000);
    }

    @Override public String getId() { return "rl_agent"; }
    @Override public String getName() { return "RL Agent"; }
    @Override public AgentCapability[] getCapabilities() {
        return new AgentCapability[]{AgentCapability.REINFORCEMENT_LEARNING, AgentCapability.PLANNING};
    }

    @Override public boolean initialize() {
        try {
            interpreter = modelManager.loadModel(MODEL_NAME);
            if (interpreter == null) return false;
            initialized = true;
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize RLAgent", e);
            return false;
        }
    }

    @Override public AgentResponse process(AgentContext context, AgentInput input) throws AgentException {
        if (!initialized) throw new AgentException(AgentException.ErrorType.CONFIGURATION_ERROR, "Agent not initialized");

        if (input.getType() == AgentInput.InputType.FEEDBACK) {
            float reward = (float) input.getParameters().getOrDefault("reward", 0.0f);
            if (lastState != null) {
                replayBuffer.add(lastState, lastAction, reward);
            }
            return AgentResponse.success("Reward processed", 1.0f);
        }

        try {
            float[] state = stateEncoder.encode(context.getAppState());
            float[][] inputArr = new float[1][STATE_DIM];
            System.arraycopy(state, 0, inputArr[0], 0, STATE_DIM);

            float[][] outputArr = new float[1][ACTION_SPACE_SIZE];
            interpreter.run(inputArr, outputArr);

            int actionIndex = 0;
            float maxQ = outputArr[0][0];
            for (int i = 1; i < ACTION_SPACE_SIZE; i++) {
                if (outputArr[0][i] > maxQ) {
                    maxQ = outputArr[0][i];
                    actionIndex = i;
                }
            }

            lastState = state;
            lastAction = actionIndex;

            Action action = decodeAction(actionIndex);

            return new AgentResponse.Builder()
                .status(AgentResponse.ResponseStatus.SUCCESS)
                .text("RL Agent suggested: " + action.getDescription())
                .confidence(Math.min(1.0f, Math.max(0.0f, maxQ)))
                .action(action)
                .metadata(Map.of("action_index", actionIndex, "q_value", maxQ))
                .build();

        } catch (Exception e) {
            throw new AgentException(AgentException.ErrorType.INFERENCE_ERROR, "DQN inference failed", e);
        }
    }

    private Action decodeAction(int actionIndex) {
        switch (actionIndex) {
            case 0: return Action.openApp("com.android.chrome");
            case 1: return Action.sendMessage("User", "Hello!");
            default: return new Action("GENERIC", "Perform action " + actionIndex, Map.of());
        }
    }

    @Override public void shutdown() { if (interpreter != null) interpreter.close(); initialized = false; }
    @Override public HealthStatus getHealthStatus() {
        return initialized ? HealthStatus.healthy() : HealthStatus.unhealthy("Not initialized");
    }
    @Override public long getEstimatedLatencyMs() { return 50; }
}
