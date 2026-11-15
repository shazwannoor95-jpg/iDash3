package com.shazwan.idash3;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class DrivePlanTest {
    private static final List<String> INSTALLED = Arrays.asList(
        "com.google.android.apps.maps/com.google.android.maps.MapsActivity", "com.waze/com.waze.FreeMapAppActivity",
        "com.google.android.youtube/.HomeActivity", "org.smarttube.stable/.Splash", "com.acloud.stub.localmusic/.Main");

    @Test public void packageOf() {
        assertEquals("com.waze", DrivePlan.packageOf("com.waze/com.waze.FreeMapAppActivity"));
        assertEquals("com.waze", DrivePlan.packageOf("com.waze"));
        assertEquals("", DrivePlan.packageOf(null));
    }
    @Test public void prefersWazeThenSmartTube() {
        assertEquals("com.waze/com.waze.FreeMapAppActivity", DrivePlan.pick(INSTALLED, DrivePlan.MAP_PREFS));
        assertEquals("org.smarttube.stable/.Splash", DrivePlan.pick(INSTALLED, DrivePlan.MEDIA_PREFS));
    }
    @Test public void fallsBackWhenPreferredMissing() {
        List<String> noWaze = Arrays.asList("com.google.android.apps.maps/.A", "com.google.android.youtube/.B");
        assertEquals("com.google.android.apps.maps/.A", DrivePlan.pick(noWaze, DrivePlan.MAP_PREFS));
        assertEquals("com.google.android.youtube/.B", DrivePlan.pick(noWaze, DrivePlan.MEDIA_PREFS));
        assertNull(DrivePlan.pick(Arrays.asList("x/.Y"), DrivePlan.MAP_PREFS));
    }
    @Test public void savedChoiceKeptOnlyWhileInstalled() {
        assertEquals("com.google.android.youtube/.HomeActivity", DrivePlan.keepOrPick("com.google.android.youtube/.HomeActivity", INSTALLED, DrivePlan.MEDIA_PREFS));
        assertEquals("org.smarttube.stable/.Splash", DrivePlan.keepOrPick("gone/.X", INSTALLED, DrivePlan.MEDIA_PREFS));
        assertEquals("org.smarttube.stable/.Splash", DrivePlan.keepOrPick("", INSTALLED, DrivePlan.MEDIA_PREFS));
    }
    @Test public void changeLayoutKeepsNavigationOrMediaCloserToDriver() {
        assertFalse(DrivePlan.mapLeft(DrivePlan.LAYOUT_NAV_CLOSER, false));   // RHD: navigation on the right
        assertTrue(DrivePlan.mapLeft(DrivePlan.LAYOUT_MEDIA_CLOSER, false));  // media on the right, map left
        assertTrue(DrivePlan.mapLeft(DrivePlan.LAYOUT_NAV_CLOSER, true));     // LHD: navigation on the left
        assertFalse(DrivePlan.mapLeft(DrivePlan.LAYOUT_MEDIA_CLOSER, true));
    }
    @Test public void oldSwapPreferenceMapsToLayoutOption() {
        assertEquals(DrivePlan.LAYOUT_MEDIA_CLOSER, DrivePlan.layoutFor(true, false));
        assertEquals(DrivePlan.LAYOUT_NAV_CLOSER, DrivePlan.layoutFor(false, false));
    }
    @Test public void firstLaunchedIsDockedLeft() {
        assertArrayEquals(new String[]{"map", "media"}, DrivePlan.launchOrder("map", "media", true));
        assertArrayEquals(new String[]{"media", "map"}, DrivePlan.launchOrder("map", "media", false));
    }
}
