package com.shazwan.idash3;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
/** Opt-in accessibility overrides; saved material and custom appearance remain untouched. */
final class Readability {
 static final String MODE="readability_mode",CONTRAST="readability_contrast",OPAQUE="readability_opaque";
 static int tone(int mode,int uiMode,int legacy){
  if(mode==1)return IosStyle.LIGHT;if(mode==2)return IosStyle.DARK;
  if(mode==3){int night=uiMode&Configuration.UI_MODE_NIGHT_MASK;if(night==Configuration.UI_MODE_NIGHT_YES)return IosStyle.DARK;if(night==Configuration.UI_MODE_NIGHT_NO)return IosStyle.LIGHT;}
  return legacy;
 }
 static GlassAppearance apply(Context c,GlassAppearance s){
  SharedPreferences p=c.getSharedPreferences("idash",0);int mode=GlassAppearance.integer(p,MODE,0);
  boolean contrast=GlassAppearance.bool(p,CONTRAST,false),opaque=GlassAppearance.bool(p,OPAQUE,false);
  if(mode==0&&!contrast&&!opaque)return s;
  return new GlassAppearance(tone(mode,c.getResources().getConfiguration().uiMode,s.tone),contrast?10:opaque?Math.min(s.transparency,20):s.transparency,s.quality,s.tint,s.reflection,s.motion,true);
 }
 private Readability(){}
}
