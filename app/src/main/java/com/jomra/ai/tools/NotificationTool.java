package com.jomra.ai.tools;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import java.util.Map;

public class NotificationTool implements Tool {
    private static final String CHANNEL_ID = "jomra_ai_notifications";
    private final Context context;

    public NotificationTool(Context context) {
        this.context = context.getApplicationContext();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Jomra AI Notifications";
            String description = "Notifications from Jomra AI Agents";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override public String getName() { return "notification"; }
    @Override public String getDescription() { return "Show a system notification"; }
    @Override public ToolParameter[] getParameters() {
        return new ToolParameter[]{
            new ToolParameter("title", "Notification title", String.class, true, "Jomra AI"),
            new ToolParameter("message", "Notification message", String.class, true, "")
        };
    }

    @Override public ToolResult execute(Map<String, Object> params) throws ToolException {
        long startTime = System.currentTimeMillis();
        String title = (String) params.getOrDefault("title", "Jomra AI");
        String message = (String) params.get("message");

        if (message == null || message.trim().isEmpty()) {
            throw new ToolException("Notification message cannot be empty");
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());

        return new ToolResult.Builder()
            .success(true)
            .resultText("Notification shown: " + title)
            .executionTime(System.currentTimeMillis() - startTime)
            .build();
    }

    @Override public long getEstimatedDurationMs() { return 50; }
    @Override public boolean isAvailable() { return true; }
}
