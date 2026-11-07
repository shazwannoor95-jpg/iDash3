package com.shazwan.idash3;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

/**
 * Accessibility service used ONLY to invoke system global actions (split-screen toggle, Back, Home,
 * Recents) for the Drive screen. It declares no event types and reads no window content.
 */
public class SplitService extends AccessibilityService {
    private static volatile SplitService instance;

    static boolean isRunning() { return instance != null; }

    /** Docks the foreground app / leaves split-screen. Returns false if the service is not enabled. */
    static boolean toggleSplit() { SplitService s = instance; return s != null && s.performGlobalAction(GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN); }
    static boolean back() { SplitService s = instance; return s != null && s.performGlobalAction(GLOBAL_ACTION_BACK); }
    static boolean home() { SplitService s = instance; return s != null && s.performGlobalAction(GLOBAL_ACTION_HOME); }
    static boolean recents() { SplitService s = instance; return s != null && s.performGlobalAction(GLOBAL_ACTION_RECENTS); }

    @Override protected void onServiceConnected() { super.onServiceConnected(); instance = this; }
    @Override public boolean onUnbind(android.content.Intent intent) { if (instance == this) instance = null; return super.onUnbind(intent); }
    @Override public void onDestroy() { if (instance == this) instance = null; super.onDestroy(); }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) { /* intentionally unused */ }
    @Override public void onInterrupt() { /* nothing to interrupt */ }
}
