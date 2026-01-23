package com.jomra.ai.moaziz;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class MoazizPolicy {
    private static final String TAG = "MoazizPolicy";
    private Map<String, Map<String, Object>> policies;
    private final Gson gson = new Gson();

    public MoazizPolicy(Context context) {
        try {
            InputStream is = context.getAssets().open("moaziz/learned_policy.json");
            Reader reader = new InputStreamReader(is);
            Type type = new TypeToken<Map<String, Map<String, Object>>>(){}.getType();
            policies = gson.fromJson(reader, type);
            Log.i(TAG, "Loaded " + (policies != null ? policies.size() : 0) + " learned policies");
        } catch (Exception e) {
            Log.e(TAG, "Failed to load Moaziz policy", e);
            policies = new HashMap<>();
        }
    }

    public Map<String, Object> getAction(String env, String complexity, String perfProfile) {
        // Construct the Python-style tuple key
        String key = String.format("('%s', '%s', '%s')", env, complexity, perfProfile);
        return policies.getOrDefault(key, new HashMap<>());
    }
}
