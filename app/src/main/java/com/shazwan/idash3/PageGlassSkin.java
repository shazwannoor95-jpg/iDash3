package com.shazwan.idash3;

import android.view.View;
import android.view.ViewGroup;

/** ZIP Drive/Apps skin: decoration only, preserving material, focus and page behaviour. */
final class PageGlassSkin {
    private PageGlassSkin() {}

    static void apply(LauncherActivity a, View shell, View body, String page) {
        if (!"drive".equals(page) && !"apps".equals(page)) return;
        if (!(shell.getBackground() instanceof ScenicBackdropDrawable))
            shell.setBackground(new ScenicBackdropDrawable(a.getResources().getDisplayMetrics().density));
        styleTree(a, shell);
    }

    static void refreshAppsGrid(LauncherActivity a, View grid) {
        if ("apps".equals(a.page)) styleTree(a, grid);
    }

    static void refreshDriveMain(LauncherActivity a, View main) {
        if ("drive".equals(a.page)) styleTree(a, main);
    }

    private static void styleTree(LauncherActivity a, View v) {
        if (v == null || v instanceof android.webkit.WebView
                || v instanceof org.osmdroid.views.MapView) return;
        // Existing foregrounds include app-tile focus/ripple; never replace them.
        // Stateful/wrapped backgrounds retain their own focus and pressed decoration.
        if (v.getBackground() instanceof GlassSurface && v.getForeground() == null) {
            GlassSurface glass = (GlassSurface) v.getBackground();
            v.setForeground(new LiquidGlassStrokeDrawable(
                    a.getResources().getDisplayMetrics().density, glass.radiusDp()));
        }
        if (v instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) v;
            for (int i = 0; i < group.getChildCount(); i++) styleTree(a, group.getChildAt(i));
        }
    }
}
