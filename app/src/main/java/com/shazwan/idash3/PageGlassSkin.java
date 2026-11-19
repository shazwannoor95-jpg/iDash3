package com.shazwan.idash3;
import android.view.View;
/** Existing page hooks now delegate to exactly one ULTRA skin session. */
final class PageGlassSkin {
 private PageGlassSkin(){}
 static void apply(LauncherActivity a,View shell,View body,String page){UltraGlassSkin.apply(a,shell,page);}
 static void refreshAppsGrid(LauncherActivity a,View grid){UltraGlassSkin.afterAppsFiltered(a,grid);}
 static void refreshDriveMain(LauncherActivity a,View main){UltraGlassSkin.afterDriveChanged(a,main);}
}
