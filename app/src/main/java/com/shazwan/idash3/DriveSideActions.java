package com.shazwan.idash3;
import android.accessibilityservice.AccessibilityService;import android.view.accessibility.AccessibilityEvent;import android.content.Intent;
/** Only the user's explicit split-screen action. No window events or content requested. */
public final class DriveSideActions extends AccessibilityService {
 private static volatile DriveSideActions service;
 static boolean ready(){return service!=null;}
 static boolean recents(){DriveSideActions s=service;return s!=null&&s.performGlobalAction(GLOBAL_ACTION_RECENTS);}
 static boolean dock(){DriveSideActions s=service;return s!=null&&s.performGlobalAction(GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN);}
 @Override protected void onServiceConnected(){service=this;}
 @Override public boolean onUnbind(Intent i){if(service==this)service=null;return super.onUnbind(i);}
 @Override public void onDestroy(){if(service==this)service=null;super.onDestroy();}
 @Override public void onAccessibilityEvent(AccessibilityEvent event){}
 @Override public void onInterrupt(){}
}
