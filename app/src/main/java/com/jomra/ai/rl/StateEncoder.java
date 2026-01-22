package com.jomra.ai.rl;

import com.jomra.ai.agents.AppState;

public class StateEncoder {
    private static final int STATE_DIM = 50;
    public float[] encode(AppState state) {
        float[] vector = new float[STATE_DIM];
        vector[0] = state.getHourOfDay() / 24f;
        vector[10] = state.getBatteryLevel() / 100f;
        return vector;
    }
}
