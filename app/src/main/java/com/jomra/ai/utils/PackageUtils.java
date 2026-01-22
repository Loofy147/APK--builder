package com.jomra.ai.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import androidx.core.content.ContextCompat;
import com.jomra.ai.R;

public class PackageUtils {
    private static final String TAG = "PackageUtils";

    public static CharSequence safeGetLabel(PackageManager pm, ApplicationInfo ai) {
        try {
            return pm.getApplicationLabel(ai);
        } catch (Throwable t) {
            Log.w(TAG, "Failed to get label for " + ai.packageName, t);
            return ai.packageName != null ? ai.packageName : "Unknown";
        }
    }

    public static Drawable safeGetIcon(Context context, PackageManager pm, ApplicationInfo ai) {
        try {
            return ai.loadIcon(pm);
        } catch (Throwable t) {
            Log.w(TAG, "Failed to load icon for " + ai.packageName, t);
            return ContextCompat.getDrawable(context, R.drawable.ic_default_app);
        }
    }
}
