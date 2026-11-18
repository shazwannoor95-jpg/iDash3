package com.shazwan.idash3;
import android.content.Context;
/** Panel material selection is independent of saved tint/transparency/quality/icon settings. */
final class GlassMaterial {
 static final String KEY="glass_material_v4";
 static final int CLASSIC=0,IOS26=1;
 private GlassMaterial(){}
 static int read(Context c){return GlassAppearance.integer(c.getSharedPreferences("idash",0),KEY,IOS26)==IOS26?IOS26:CLASSIC;}
 static void save(Context c,int material){c.getSharedPreferences("idash",0).edit().putInt(KEY,material==IOS26?IOS26:CLASSIC).apply();}
 static String label(Context c){return read(c)==IOS26?"iOS 26":"Classic iDash3";}
}
