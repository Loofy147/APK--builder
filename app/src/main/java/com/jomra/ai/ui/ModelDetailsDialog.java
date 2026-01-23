package com.jomra.ai.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.jomra.ai.R;
import com.jomra.ai.models.*;

public class ModelDetailsDialog extends Dialog {
    private final ModelInfo model;
    private final ModelCatalog catalog;

    public ModelDetailsDialog(Context context, ModelInfo model, ModelCatalog catalog, LargeModelManager ignored) {
        super(context);
        this.model = model;
        this.catalog = catalog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_model_details);

        TextView tvName = findViewById(R.id.tvName);
        TextView tvDescription = findViewById(R.id.tvDescription);
        TextView tvVersion = findViewById(R.id.tvVersion);
        TextView tvAuthor = findViewById(R.id.tvAuthor);
        TextView tvLicense = findViewById(R.id.tvLicense);
        TextView tvSize = findViewById(R.id.tvSize);
        TextView tvCapabilities = findViewById(R.id.tvCapabilities);
        TextView tvRequirements = findViewById(R.id.tvRequirements);
        Button btnDownload = findViewById(R.id.btnDownload);
        Button btnCancel = findViewById(R.id.btnCancel);

        tvName.setText(model.name);
        tvDescription.setText(model.description);
        tvVersion.setText("Version " + model.version);
        tvAuthor.setText("By " + model.author);
        tvLicense.setText("License: " + model.license);
        tvSize.setText(String.format("%.1f MB", model.sizeBytes / (1024.0 * 1024)));
        String caps = "N/A";
        if (model.capabilities != null && !model.capabilities.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < model.capabilities.size(); i++) {
                sb.append(model.capabilities.get(i));
                if (i < model.capabilities.size() - 1) sb.append(", ");
            }
            caps = sb.toString();
        }
        tvCapabilities.setText("Capabilities: " + caps);
        tvRequirements.setText(String.format("Requires: Android %d+, %d MB RAM", model.minAndroidVersion, model.minRamMB));

        if (catalog.isModelDownloaded(model.id)) {
            btnDownload.setText("Already Downloaded");
            btnDownload.setEnabled(false);
        } else {
            btnDownload.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), DownloadService.class);
                intent.putExtra("model_id", model.id);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    getContext().startForegroundService(intent);
                } else {
                    getContext().startService(intent);
                }
                Toast.makeText(getContext(), "Download started in background", Toast.LENGTH_SHORT).show();
                dismiss();
            });
        }
        btnCancel.setOnClickListener(v -> dismiss());
    }
}
