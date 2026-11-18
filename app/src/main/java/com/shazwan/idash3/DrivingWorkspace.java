package com.shazwan.idash3;

import android.app.AlertDialog;
import android.content.*;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Optional flat driving workspace. Does not draw or modify any glass surfaces. */
final class DrivingWorkspace extends LinearLayout {
 final LauncherActivity a;
 final LinearLayout content,tabs;
 final TextView[] tabButtons=new TextView[DrivingTemplates.TAB_COUNT];
 ControlMedia media;
 AppLibraryGrid appGrid;
 AlertDialog alert;
 boolean closed;
 private static final String[] PHONE={"com.autochips.bluetooth","com.android.dialer","com.android.contacts","com.google.android.dialer"};
 DrivingWorkspace(LauncherActivity owner){
  super(owner);a=owner;setOrientation(HORIZONTAL);setContentDescription("Driving workspace");
  LinearLayout rail=new LinearLayout(a);rail.setOrientation(VERTICAL);rail.setGravity(Gravity.CENTER);rail.setBackgroundColor(0xe61a2233);
  addView(rail,new LayoutParams(a.dp(88),-1));
  TextClock clock=new TextClock(a);clock.setFormat24Hour("HH:mm");clock.setFormat12Hour("h:mm a");clock.setTextColor(Color.WHITE);clock.setTextSize(18);clock.setGravity(Gravity.CENTER);clock.setSingleLine(true);rail.addView(clock,new LayoutParams(-1,a.dp(40)));
  rail.addView(railIcon("Peta","maps",()->launch("drive_map",DrivePlan.MAP_PREFS)),new LayoutParams(-1,a.dp(DrivingTemplates.TOUCH_DP)));
  rail.addView(railIcon("Media","music",()->launch("drive_media",DrivePlan.MEDIA_PREFS)),new LayoutParams(-1,a.dp(DrivingTemplates.TOUCH_DP)));
  rail.addView(railIcon("Telefon","phone",()->launch("",PHONE)),new LayoutParams(-1,a.dp(DrivingTemplates.TOUCH_DP)));
  rail.addView(railIcon("Home",null,()->{a.page="home";a.query="";a.render();}),new LayoutParams(-1,a.dp(DrivingTemplates.TOUCH_DP)));
  LinearLayout main=new LinearLayout(a);main.setOrientation(VERTICAL);main.setPadding(a.dp(8),0,0,0);addView(main,new LayoutParams(0,-1,1));
  tabs=new LinearLayout(a);main.addView(tabs,new LayoutParams(-1,a.dp(DrivingTemplates.TOUCH_DP)));
  String[] names={"Apps","Now Playing","Tindakan"};for(int i=0;i<names.length;i++){final int selected=i;tabButtons[i]=button(names[i],()->select(selected));tabButtons[i].setContentDescription("Driving tab "+names[i]);tabs.addView(tabButtons[i],new LayoutParams(0,-1,1));}
  content=new LinearLayout(a);content.setOrientation(VERTICAL);main.addView(content,new LayoutParams(-1,0,1));select(a.drivingTab);
 }
 View railIcon(String label,String icon,Runnable action){
  android.widget.FrameLayout hit=new android.widget.FrameLayout(a);hit.setContentDescription("Driving "+label);hit.setFocusable(true);hit.setBackground(buttonBackground());hit.setOnClickListener(v->action.run());hit.setTooltipText(label);
  android.graphics.Bitmap bitmap=Ios26Icons.named(a,icon);
  View image;if(bitmap!=null){ImageView v=new ImageView(a);v.setImageBitmap(bitmap);v.setScaleType(ImageView.ScaleType.FIT_CENTER);image=v;}else image=new DockGlyph(a,DockGlyph.APPS);
  image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);hit.addView(image,new android.widget.FrameLayout.LayoutParams(a.dp(40),a.dp(40),Gravity.CENTER));return hit;
 }
 TextView text(String value,int size){TextView v=new TextView(a);v.setText(value);v.setTextColor(Color.WHITE);v.setTextSize(size);v.setGravity(Gravity.CENTER);v.setSingleLine(true);v.setEllipsize(TextUtils.TruncateAt.END);return v;}
 TextView button(String label,Runnable action){TextView v=text(label,16);v.setMinHeight(a.dp(DrivingTemplates.TOUCH_DP));v.setPadding(a.dp(6),0,a.dp(6),0);v.setFocusable(true);v.setBackground(buttonBackground());v.setContentDescription("Driving "+label);v.setOnClickListener(w->action.run());return v;}
 android.graphics.drawable.Drawable buttonBackground(){
  android.graphics.drawable.StateListDrawable states=new android.graphics.drawable.StateListDrawable();
  int[][] flags={{android.R.attr.state_pressed},{android.R.attr.state_focused},{android.R.attr.state_selected},{}};
  int[] colors={0xff436f97,0xff375f83,0xff29435f,0xff172335};
  for(int i=0;i<flags.length;i++){android.graphics.drawable.GradientDrawable fill=new android.graphics.drawable.GradientDrawable();fill.setColor(colors[i]);fill.setCornerRadius(a.dp(8));if(i==1)fill.setStroke(a.dp(2),Color.WHITE);states.addState(flags[i],fill);}return states;
 }
 void select(int requested){
  if(closed)return;pause();if(alert!=null){alert.dismiss();alert=null;}media=null;appGrid=null;
  a.drivingTab=DrivingTemplates.tab(requested);content.removeAllViews();
  for(int i=0;i<tabButtons.length;i++){tabButtons[i].setSelected(i==a.drivingTab);}
  if(a.drivingTab==DrivingTemplates.APPS){appGrid=new AppLibraryGrid(a);appGrid.setApps(a.apps);content.addView(appGrid,new LayoutParams(-1,-1));}
  else if(a.drivingTab==DrivingTemplates.NOW_PLAYING)nowPlaying();else actions();
 }
 void nowPlaying(){
  final ControlMedia controls=new ControlMedia(a,null);media=controls;
  controls.title=text("Not Playing",24);controls.artist=text("",16);controls.status=text("",14);
  content.addView(controls.title,new LayoutParams(-1,a.dp(36)));content.addView(controls.artist,new LayoutParams(-1,a.dp(28)));content.addView(controls.status,new LayoutParams(-1,a.dp(24)));
  LinearLayout transport=new LinearLayout(a);content.addView(transport,new LayoutParams(-1,a.dp(72)));
  for(int action:new int[]{6,7,8}){
   LinearLayout control=new LinearLayout(a);control.setOrientation(VERTICAL);control.setGravity(Gravity.CENTER);ControlGlyph glyph=new ControlGlyph(a,action);control.addView(glyph,new LayoutParams(a.dp(32),a.dp(32)));control.addView(text(action==6?"Previous":action==7?"Play":"Next",16),new LayoutParams(-1,a.dp(24)));
   control.setFocusable(true);control.setBackground(buttonBackground());control.setOnClickListener(v->controls.action(action));transport.addView(control,new LayoutParams(0,-1,1));
   if(action==6)controls.previous=control;else if(action==7){controls.play=control;controls.playGlyph=glyph;}else controls.next=control;
  }
  content.addView(button("Akses media",()->controls.explain()),new LayoutParams(-1,a.dp(DrivingTemplates.TOUCH_DP)));
  content.addView(button("Buka app media",()->launch("drive_media",DrivePlan.MEDIA_PREFS)),new LayoutParams(-1,a.dp(DrivingTemplates.TOUCH_DP)));
  controls.render();if(a.resumed)resume();
 }
 void actions(){
  content.addView(button("Buka peta",()->launch("drive_map",DrivePlan.MAP_PREFS)),new LayoutParams(-1,a.dp(DrivingTemplates.TOUCH_DP)));
  content.addView(button("Buka media",()->launch("drive_media",DrivePlan.MEDIA_PREFS)),new LayoutParams(-1,a.dp(DrivingTemplates.TOUCH_DP)));
  content.addView(button("Telefon / Bluetooth",()->launch("",PHONE)),new LayoutParams(-1,a.dp(DrivingTemplates.TOUCH_DP)));
  content.addView(button("Drive Dashboard",()->{a.page="drive";a.render();}),new LayoutParams(-1,a.dp(DrivingTemplates.TOUCH_DP)));
 }
 void launch(String pref,String[] preferred){
  ArrayList<String> installed=new ArrayList<>();for(LauncherActivity.App app:a.apps)installed.add(app.component);
  String component=DrivingTemplates.choose(pref.isEmpty()?null:a.prefs.getString(pref,null),installed,preferred);
  if(component!=null){for(LauncherActivity.App app:a.apps)if(component.equals(app.component)){a.launch(app);return;}}
  if(alert!=null)alert.dismiss();alert=new AlertDialog.Builder(a).setTitle(a.loaded?"App belum tersedia":"App masih dimuatkan").setMessage("Pilih atau pasang app yang sesuai melalui App Library. Kawalan peta dan widget ada pada Drive Dashboard.").setNegativeButton("Tutup",null).setPositiveButton("App Library",(d,w)->{a.page="apps";a.query="";a.render();}).create();alert.show();
 }
 void appsChanged(){if(!closed&&appGrid!=null)appGrid.setApps(a.apps);}
 void resume(){if(!closed&&media!=null)media.resume();}
 void pause(){if(media!=null)media.pause();if(alert!=null){alert.dismiss();alert=null;}}
 void close(){if(closed)return;closed=true;pause();if(alert!=null){alert.dismiss();alert=null;}removeAllViews();}
}
