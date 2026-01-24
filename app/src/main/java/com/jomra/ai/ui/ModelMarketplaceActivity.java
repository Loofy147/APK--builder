package com.jomra.ai.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.jomra.ai.R;
import com.jomra.ai.models.*;
import java.util.*;

public class ModelMarketplaceActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;

    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private SearchView searchView;
    private ChipGroup chipGroup;
    private TextView tvEmpty;
    private ProgressBar progressBar;

    private ModelCatalog catalog;
    private ModelAdapter adapter;
    private List<ModelInfo> currentModels = new ArrayList<>();
    private ModelCategory selectedCategory = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_marketplace);

        checkPermissions();
        initializeViews();
        initializeData();
        setupListeners();
        refreshCatalog();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void initializeViews() {
        tabLayout = findViewById(R.id.tabLayout);
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        searchView = findViewById(R.id.searchView);
        chipGroup = findViewById(R.id.chipGroup);
        tvEmpty = findViewById(R.id.tvEmpty);
        progressBar = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tabLayout.addTab(tabLayout.newTab().setText("All"));
        tabLayout.addTab(tabLayout.newTab().setText("Featured"));
        tabLayout.addTab(tabLayout.newTab().setText("Downloaded"));

        for (ModelCategory category : ModelCategory.values()) {
            Chip chip = new Chip(this);
            chip.setText(category.displayName);
            chip.setCheckable(true);
            chip.setTag(category);
            chipGroup.addView(chip);
        }
    }

    private void initializeData() {
        catalog = ModelCatalog.getInstance(this);
        adapter = new ModelAdapter(this, currentModels, catalog, this::onModelClick);
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { filterModels(tab.getPosition()); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedCategory = null;
            } else {
                int chipId = checkedIds.get(0);
                Chip chip = findViewById(chipId);
                selectedCategory = (ModelCategory) chip.getTag();
            }
            filterModels(tabLayout.getSelectedTabPosition());
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { searchModels(query); return true; }
            @Override public boolean onQueryTextChange(String newText) { if (newText.isEmpty()) filterModels(tabLayout.getSelectedTabPosition()); return true; }
        });

        swipeRefresh.setOnRefreshListener(this::refreshCatalog);
    }

    private void refreshCatalog() {
        swipeRefresh.setRefreshing(true);
        catalog.refreshCatalog(new CatalogCallback() {
            @Override
            public void onSuccess(List<ModelInfo> models) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    filterModels(tabLayout.getSelectedTabPosition());
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(ModelMarketplaceActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void filterModels(int tabPosition) {
        List<ModelInfo> filtered;
        switch (tabPosition) {
            case 0: filtered = (selectedCategory != null) ? catalog.getModelsByCategory(selectedCategory) : getAllModels(); break;
            case 1: filtered = getFeaturedModels(); break;
            case 2: filtered = catalog.getDownloadedModels(); break;
            default: filtered = new ArrayList<>();
        }
        currentModels = filtered;
        updateUI();
    }

    private List<ModelInfo> getAllModels() {
        List<ModelInfo> all = new ArrayList<>();
        for (ModelCategory cat : ModelCategory.values()) all.addAll(catalog.getModelsByCategory(cat));
        return all;
    }

    private List<ModelInfo> getFeaturedModels() {
        List<ModelInfo> featured = new ArrayList<>();
        for (ModelInfo m : getAllModels()) if (m.featured) featured.add(m);
        return featured;
    }

    private void searchModels(String query) {
        currentModels = catalog.searchModels(query);
        updateUI();
    }

    private void updateUI() {
        adapter.updateModels(currentModels);
        tvEmpty.setVisibility(currentModels.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(currentModels.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void onModelClick(ModelInfo model) {
        new ModelDetailsDialog(this, model, catalog, null).show();
    }
}
