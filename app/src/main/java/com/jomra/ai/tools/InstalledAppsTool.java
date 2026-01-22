package com.jomra.ai.tools;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import com.jomra.ai.utils.PackageUtils;
import java.util.*;

public class InstalledAppsTool implements Tool {
    private static final String TAG = "InstalledAppsTool";
    private final Context context;

    public InstalledAppsTool(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override public String getName() { return "list_apps"; }
    @Override public String getDescription() { return "List all installed launcher applications"; }
    @Override public ToolParameter[] getParameters() { return new ToolParameter[0]; }

    @Override public ToolResult execute(Map<String, Object> params) throws ToolException {
        long startTime = System.currentTimeMillis();

        try {
            PackageManager pm = context.getPackageManager();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
            StringBuilder result = new StringBuilder("Installed Apps:\n");
            List<Map<String, String>> appData = new ArrayList<>();

            for (ResolveInfo ri : activities) {
                try {
                    String pkg = ri.activityInfo.packageName;
                    ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
                    String label = PackageUtils.safeGetLabel(pm, ai).toString();

                    result.append("- ").append(label).append(" (").append(pkg).append(")\n");

                    Map<String, String> app = new HashMap<>();
                    app.put("label", label);
                    app.put("package", pkg);
                    appData.add(app);
                } catch (Throwable t) {
                    // Safe skip
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("apps", appData);
            data.put("count", appData.size());

            return new ToolResult.Builder()
                .success(true)
                .resultText(result.toString())
                .data(data)
                .executionTime(System.currentTimeMillis() - startTime)
                .build();

        } catch (Exception e) {
            throw new ToolException("Failed to list apps: " + e.getMessage());
        }
    }

    @Override public long getEstimatedDurationMs() { return 1000; }
    @Override public boolean isAvailable() { return true; }
}
