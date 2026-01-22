package com.jomra.ai.tools;

import android.content.Context;
import android.util.Log;
import java.util.*;

public class ToolRegistry {
    private static final String TAG = "ToolRegistry";
    private final Map<String, Tool> tools;
    private final Context context;

    public ToolRegistry(Context context) {
        this.context = context.getApplicationContext();
        this.tools = new HashMap<>();
        registerDefaultTools();
    }

    private void registerDefaultTools() {
        registerTool(new CalculatorTool());
        registerTool(new SystemInfoTool(context));
        registerTool(new WebSearchTool(context));
    }

    public void registerTool(Tool tool) {
        tools.put(tool.getName(), tool);
        Log.i(TAG, "Registered tool: " + tool.getName());
    }

    public Tool getTool(String name) { return tools.get(name); }

    public Collection<Tool> getAvailableTools() {
        List<Tool> available = new ArrayList<>();
        for (Tool tool : tools.values()) {
            if (tool.isAvailable()) available.add(tool);
        }
        return available;
    }
}
