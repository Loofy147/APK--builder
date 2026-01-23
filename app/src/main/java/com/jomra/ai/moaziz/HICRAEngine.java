package com.jomra.ai.moaziz;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Layer 4: Hierarchical RL Core (HICRA).
 * Hierarchy-Aware Credit Assignment.
 */
public class HICRAEngine {
    private static final String TAG = "HICRAEngine";
    private final float amplificationFactor = 3.0f;

    public static class Step {
        public String content;
        public String type;
        public boolean isDecisionPoint;

        public Step(String content, String type, boolean isDecisionPoint) {
            this.content = content;
            this.type = type;
            this.isDecisionPoint = isDecisionPoint;
        }
    }

    public List<Integer> identifyPlanningSteps(List<Step> trajectory) {
        List<Integer> planningIndices = new ArrayList<>();
        String[] markers = {"<thought>", "plan:", "strategy:", "analyze"};

        for (int i = 0; i < trajectory.size(); i++) {
            Step step = trajectory.get(i);
            String content = step.content.toLowerCase();
            String type = step.type.toLowerCase();

            boolean isStrategic = step.isDecisionPoint ||
                                  type.contains("planning") ||
                                  type.contains("reasoning");

            if (!isStrategic) {
                for (String marker : markers) {
                    if (content.contains(marker)) {
                        isStrategic = true;
                        break;
                    }
                }
            }

            if (isStrategic) {
                planningIndices.add(i);
            }
        }
        return planningIndices;
    }

    public float computeAmplifiedReward(float baseReward, boolean isPlanningStep) {
        if (isPlanningStep) {
            return baseReward * amplificationFactor;
        }
        return baseReward;
    }
}
