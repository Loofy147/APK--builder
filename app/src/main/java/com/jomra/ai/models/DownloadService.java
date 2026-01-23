package com.jomra.ai.models;

import android.app.*;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import java.io.File;

public class DownloadService extends Service {
    private static final String CHANNEL_ID = "download_channel";
    private static final int NOTIFICATION_ID = 1;
    private LargeModelManager modelManager;

    @Override
    public void onCreate() {
        super.onCreate();
        modelManager = new LargeModelManager(this);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String modelId = intent.getStringExtra("model_id");
        if (modelId != null) {
            startDownload(modelId);
        }
        return START_NOT_STICKY;
    }

    private void startDownload(String modelId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle("Downloading Model")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setOnlyAlertOnce(true);

        startForeground(NOTIFICATION_ID, builder.build());

        modelManager.downloadModel(modelId, new DownloadListener() {
            @Override
            public void onStarted(ModelInfo model) {
                builder.setContentText("Downloading " + model.name);
                updateNotification(builder);
            }

            @Override
            public void onProgress(ModelInfo model, long downloaded, long total, float progress) {
                builder.setProgress(100, (int)(progress * 100), false);
                builder.setContentText(String.format("Downloading %s: %.1f%%", model.name, progress * 100));
                updateNotification(builder);
            }

            @Override
            public void onCompleted(ModelInfo model, File file) {
                builder.setContentText("Download Complete: " + model.name)
                        .setProgress(0, 0, false)
                        .setOngoing(false);
                updateNotification(builder);
                stopForeground(false);
                stopSelf();
            }

            @Override
            public void onError(String error) {
                builder.setContentText("Download Failed: " + error)
                        .setProgress(0, 0, false)
                        .setOngoing(false);
                updateNotification(builder);
                stopForeground(false);
                stopSelf();
            }
        });
    }

    private void updateNotification(NotificationCompat.Builder builder) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Model Downloads", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
