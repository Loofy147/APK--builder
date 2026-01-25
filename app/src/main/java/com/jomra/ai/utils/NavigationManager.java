package com.jomra.ai.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;
import com.jomra.ai.R;
import com.jomra.ai.ui.ModelMarketplaceActivity;

public class NavigationManager {
    private static final String TAG = "NavigationManager";
    private final Context context;
    private final DrawerLayout drawerLayout;

    public NavigationManager(Context context, DrawerLayout drawerLayout) {
        this.context = context;
        this.drawerLayout = drawerLayout;
    }

    public boolean handleNavigation(int itemId) {
        if (itemId == R.id.nav_marketplace) {
            context.startActivity(new Intent(context, ModelMarketplaceActivity.class));
        } else if (itemId == R.id.nav_settings) {
            // Future settings activity
            Log.i(TAG, "Settings selected");
        }

        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }
}
