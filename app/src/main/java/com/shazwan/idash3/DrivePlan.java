package com.shazwan.idash3;

import java.util.List;

/**
 * Pure decisions for the Android-Auto-style "Drive" screen (JVM-testable): which installed apps are
 * suggested for the map side and the media side, and in which order they must be launched so the
 * map ends up on the requested side.
 */
final class DrivePlan {
    /** Navigation apps in order of preference (lightest/most useful first on a 1 GB head unit). */
    static final String[] MAP_PREFS = {"com.waze", "com.google.android.apps.maps", "com.pve.naviguide", "com.here.app.maps", "net.osmand", "app.organicmaps"};
    /** Media apps: YouTube clients first, then music/radio. */
    static final String[] MEDIA_PREFS = {"org.smarttube.stable", "com.liskovsoft.smarttubetv.beta", "com.liskovsoft.smarttubetv", "org.schabi.newpipe", "com.google.android.youtube",
        "com.spotify.music", "org.videolan.vlc", "com.acloud.stub.localmusic", "com.acloud.stub.localradio", "com.acloud.stub.video"};

    private DrivePlan() {}

    static String packageOf(String component) {
        if (component == null) return "";
        int slash = component.indexOf('/');
        return slash > 0 ? component.substring(0, slash) : component;
    }

    /** First installed component whose package is the most preferred; null if none. */
    static String pick(List<String> installedComponents, String[] preferredPackages) {
        for (String pkg : preferredPackages)
            for (String c : installedComponents) if (pkg.equals(packageOf(c))) return c;
        return null;
    }

    /** Keeps a saved choice only while it is still installed. */
    static String keepOrPick(String saved, List<String> installed, String[] prefs) {
        if (saved != null && !saved.isEmpty() && installed.contains(saved)) return saved;
        return pick(installed, prefs);
    }

    /**
     * Split-screen docks the FIRST launched app on the left (landscape). Returns {first, second}:
     * the app to launch and dock first, then the app launched into the other side.
     */
    static String[] launchOrder(String map, String media, boolean mapOnLeft) {
        return mapOnLeft ? new String[]{map, media} : new String[]{media, map};
    }
}
