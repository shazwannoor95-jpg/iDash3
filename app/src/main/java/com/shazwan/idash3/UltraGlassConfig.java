package com.shazwan.idash3;

/**
 * Experimental MAX visual profile. No CPU, RAM, or frame-time auto-downgrades.
 * Smooth Transitions/system animation settings and lifecycle still apply.
 * Full AGSL effect requires Android 13 (API 33+) and hardware accelerated Canvas.
 * This deliberately ignores GlassPerf.low(), unlike the stock iDash3 profile.
 */
final class UltraGlassConfig {
    private UltraGlassConfig() {}

    static final boolean ENABLE_AGSL = true;
    static final boolean ENABLE_ANIMATION = true;
    static final boolean ENABLE_STATIC_HALOS = true;
    static final boolean ENABLE_CAUSTICS = true;
    static final boolean SCENE_FOR_DRIVE = true; // Decorative scene BEHIND map, not on map.
    static final boolean SCENE_FOR_APPS = false; // Abstract blue/magenta atmosphere.

    static final long FRAME_TIME_MS = 16L;       // ~60 fps, maximum appearance mode.
    static final float GLOW_RADIUS_DP = 12f;
    static final float BEVEL_WIDTH_DP = 1.3f;
    static final float SCENIC_DIM_AMOUNT = 0.12f;
}
