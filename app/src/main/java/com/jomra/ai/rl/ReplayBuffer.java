package com.jomra.ai.rl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReplayBuffer {
    private final List<Transition> buffer;
    private final int maxSize;
    public ReplayBuffer(int maxSize) { this.buffer = new ArrayList<>(); this.maxSize = maxSize; }
    public void add(float[] state, int action, float reward) {
        buffer.add(new Transition(state, action, reward));
        if (buffer.size() > maxSize) buffer.remove(0);
    }
    public int size() { return buffer.size(); }
    public static class Transition {
        public final float[] state;
        public final int action;
        public final float reward;
        public Transition(float[] s, int a, float r) { this.state = s; this.action = a; this.reward = r; }
    }
}
