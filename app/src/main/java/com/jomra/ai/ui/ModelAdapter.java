package com.jomra.ai.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.jomra.ai.R;
import com.jomra.ai.models.ModelCatalog;
import com.jomra.ai.models.ModelInfo;
import java.util.List;

public class ModelAdapter extends RecyclerView.Adapter<ModelAdapter.ViewHolder> {
    private final Context context;
    private List<ModelInfo> models;
    private final ModelCatalog catalog;
    private final OnModelClickListener listener;

    public interface OnModelClickListener { void onModelClick(ModelInfo model); }

    public ModelAdapter(Context context, List<ModelInfo> models, ModelCatalog catalog, OnModelClickListener listener) {
        this.context = context;
        this.models = models;
        this.catalog = catalog;
        this.listener = listener;
    }

    public void updateModels(List<ModelInfo> newModels) {
        this.models = newModels;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_model, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ModelInfo model = models.get(position);
        holder.tvName.setText(model.name);
        holder.tvDescription.setText(model.description);
        holder.tvSize.setText(String.format("%.1f MB", model.sizeBytes / (1024.0 * 1024)));
        holder.tvCategory.setText(model.category.displayName);
        holder.ratingBar.setRating(model.rating);
        holder.badgeFeatured.setVisibility(model.featured ? View.VISIBLE : View.GONE);

        Glide.with(context)
                .load(model.metadata != null ? model.metadata.get("icon_url") : null)
                .placeholder(R.drawable.ic_default_app)
                .into(holder.ivModel);

        if (catalog.isModelDownloaded(model.id)) {
            holder.btnDownload.setText("Downloaded");
            holder.btnDownload.setEnabled(false);
        } else {
            holder.btnDownload.setText("View Details");
            holder.btnDownload.setEnabled(true);
        }
        holder.itemView.setOnClickListener(v -> listener.onModelClick(model));
    }

    @Override public int getItemCount() { return models.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvSize, tvCategory;
        ImageView ivModel;
        RatingBar ratingBar;
        Button btnDownload;
        View badgeFeatured;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            ivModel = itemView.findViewById(R.id.ivModel);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            btnDownload = itemView.findViewById(R.id.btnDownload);
            badgeFeatured = itemView.findViewById(R.id.badgeFeatured);
        }
    }
}
