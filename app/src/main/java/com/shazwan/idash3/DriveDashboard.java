package com.shazwan.idash3;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.Marker;
import java.io.File;
import java.text.DateFormat;
import java.util.*;

/** Native map + callback media + timetable widgets. No split-screen or invented route/vehicle data. */
final class DriveDashboard extends LinearLayout implements AutoCloseable {
 static final int LOCATION_REQUEST=74;
 final LauncherActivity a;final ControlMedia media;
 final TextView prayer,prayerDetail,calendar,locationStatus,credit,locationButton;
 final FrameLayout mapHost;DashWebPlayer web;DashFmPanel fm;ControlMedia largeMedia;android.app.AlertDialog chooser;int mode;
 MapView map;Marker marker;LocationManager locations;
 boolean active,closed,listening,follow;
 long minute=Long.MIN_VALUE;String zone="",loaded="";boolean loading;Object timetable;
 final LocationListener position=new LocationListener(){
  public void onLocationChanged(Location loc){if(!active||closed||map==null)return;if(SystemClock.elapsedRealtimeNanos()-loc.getElapsedRealtimeNanos()>120000000000L){locationStatus.setText("Menunggu lokasi terkini…");return;}GeoPoint point=new GeoPoint(loc.getLatitude(),loc.getLongitude());if(!map.getOverlays().contains(marker))map.getOverlays().add(marker);marker.setPosition(point);marker.setTitle("Lokasi peranti");if(follow)map.getController().animateTo(point);map.invalidate();locationStatus.setText("Lokasi peranti · ±"+Math.round(loc.getAccuracy())+" m");}
  public void onProviderEnabled(String p){}public void onProviderDisabled(String p){if(active)locationStatus.setText("Sumber lokasi terhenti");}public void onStatusChanged(String p,int s,Bundle b){}
 };
 DriveDashboard(LauncherActivity owner){
  super(owner);a=owner;setOrientation(HORIZONTAL);setContentDescription("Drive Dashboard");
  LinearLayout rail=a.column();rail.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);rail.setBackground(a.glass(0xff25324d,18));addView(rail,new LayoutParams(a.dp(64),-1));
  TextClock clock=new TextClock(a);clock.setFormat24Hour(a.prefs.getBoolean("24h",true)?"HH:mm":"h:mm a");clock.setFormat12Hour(a.prefs.getBoolean("24h",true)?"HH:mm":"h:mm a");clock.setTextColor(Color.WHITE);clock.setTextSize(15);clock.setGravity(Gravity.CENTER);rail.addView(clock,new LayoutParams(-1,a.dp(36)));
  rail.addView(icon("Peta","maps",()->setMode(0)),new LayoutParams(-1,a.dp(48)));
  rail.addView(icon("Apps","appstore",()->{a.page="apps";a.render();}),new LayoutParams(-1,a.dp(48)));
  rail.addView(icon("Media","music",()->setMode(1)),new LayoutParams(-1,a.dp(48)));
  rail.addView(icon("Telefon","phone",()->launch(new String[]{"com.autochips.bluetooth","com.android.dialer","com.google.android.dialer","com.android.contacts"})),new LayoutParams(-1,a.dp(48)));
  rail.addView(new View(a),new LayoutParams(-1,0,1));rail.addView(icon("Home",null,()->{a.page="home";a.render();}),new LayoutParams(-1,a.dp(48)));
  LinearLayout mapArea=a.column();LayoutParams mp=new LayoutParams(0,-1,.65f);mp.setMargins(a.dp(8),0,a.dp(8),0);addView(mapArea,mp);
  LinearLayout heading=a.row();TextView title=text("Drive Dashboard",17);heading.addView(title,new LayoutParams(0,a.dp(48),1));locationButton=button("Lokasi",this::enableLocation);heading.addView(locationButton,new LayoutParams(a.dp(72),a.dp(48)));heading.addView(button("Mod",this::chooseMode),new LayoutParams(a.dp(56),a.dp(48)));mapArea.addView(heading);
  locationStatus=text("Peta kawasan · bukan lokasi anda",12);locationStatus.setContentDescription("Dashboard location status");mapArea.addView(locationStatus,new LayoutParams(-1,a.dp(28)));
  mapHost=new FrameLayout(a);mapHost.setContentDescription("Native map area");mapArea.addView(mapHost,new LayoutParams(-1,0,1));
  credit=text("© OpenStreetMap contributors · Internet untuk kawasan baharu",10);credit.setSingleLine(false);mapArea.addView(credit,new LayoutParams(-1,-2));
  ScrollView scroll=new ScrollView(a);scroll.setFillViewport(true);addView(scroll,new LayoutParams(0,-1,.35f));LinearLayout cards=a.column();scroll.addView(cards,new ScrollView.LayoutParams(-1,-2));
  LinearLayout mediaCard=card(cards);media=new ControlMedia(a,null);media.title=text("Not Playing",19);media.artist=text("",14);media.status=text("",11);mediaCard.addView(media.title);mediaCard.addView(media.artist);mediaCard.addView(media.status);
  LinearLayout transport=a.row();mediaCard.addView(transport,new LayoutParams(-1,a.dp(48)));
  for(int action:new int[]{6,7,8}){LinearLayout control=a.column();control.setGravity(Gravity.CENTER);ControlGlyph glyph=new ControlGlyph(a,action);control.addView(glyph,new LayoutParams(a.dp(24),a.dp(24)));control.addView(text(action==6?"Previous":action==7?"Play":"Next",11),new LayoutParams(-1,a.dp(20)));control.setFocusable(true);control.setBackground(buttonBackground());control.setOnClickListener(v->media.action(action));transport.addView(control,new LayoutParams(0,-1,1));if(action==6)media.previous=control;else if(action==7){media.play=control;media.playGlyph=glyph;}else media.next=control;}
  mediaCard.addView(button("Akses media",media::explain),new LayoutParams(-1,a.dp(48)));media.render();
  LinearLayout prayerCard=card(cards);prayer=text("Waktu solat",19);prayer.setSingleLine(false);prayer.setContentDescription("Live prayer countdown");prayerDetail=text("",12);prayerDetail.setSingleLine(false);prayerCard.addView(prayer);prayerCard.addView(prayerDetail);prayerCard.addView(button("Zon / jadual",a::jakimDialog),new LayoutParams(-1,a.dp(48)));
  LinearLayout dateCard=card(cards);calendar=text("",16);calendar.setSingleLine(false);dateCard.addView(calendar);dateCard.setContentDescription("Dashboard calendar");dateCard.setOnClickListener(v->a.showCalendar());dateCard.setFocusable(true);dateCard.setMinimumHeight(a.dp(64));
  setMode(GlassAppearance.integer(a.prefs,"dashboard_panel",0));a.loadJakim(false);tick(System.currentTimeMillis());if(a.resumed)resume();
 }
 void chooseMode(){chooser=new android.app.AlertDialog.Builder(a).setTitle("Paparan dashboard").setSingleChoiceItems(new String[]{"Peta","Audio / media player","Radio FM","YouTube web"},mode,(d,i)->{d.dismiss();setMode(i);}).setNegativeButton("Tutup",null).show();}
 void disposeMain(){stopLocation();if(web!=null){web.close();web=null;}if(fm!=null){fm.close();fm=null;}if(largeMedia!=null){largeMedia.pause();largeMedia=null;}if(map!=null){map.onPause();map.onDetach();map=null;}mapHost.removeAllViews();}
 void setMode(int requested){
  if(closed)return;disposeMain();mode=Math.max(0,Math.min(3,requested));mapHost.setContentDescription(mode==0?"Native map area":mode==1?"Dashboard audio":mode==2?"Dashboard FM":"Dashboard YouTube");a.prefs.edit().putInt("dashboard_panel",mode).apply();locationButton.setVisibility(mode==0?VISIBLE:GONE);credit.setVisibility(mode==0?VISIBLE:GONE);
  if(mode==0){locationStatus.setText("Peta kawasan · bukan lokasi anda");createMap();if(active&&map!=null){map.onResume();if(a.prefs.getBoolean("dashboard_location",false))startLocation();}}
  else if(mode==3){locationStatus.setText("YouTube web");web=new DashWebPlayer(a,640,360);mapHost.addView(web,new FrameLayout.LayoutParams(-1,-1));if(active)web.resume();}
  else if(mode==2){locationStatus.setText("Radio FM · antena head unit");fm=new DashFmPanel(a,a.getResources().getDisplayMetrics().density,()->{chooser=new android.app.AlertDialog.Builder(a).setTitle("FM report").setMessage(DashFmController.report(a)).setPositiveButton("Tutup",null).show();});ResolutionStage stage=new ResolutionStage(a,560,330);stage.addView(fm);mapHost.addView(stage,new FrameLayout.LayoutParams(-1,-1));if(active)fm.resume();}
  else {locationStatus.setText("Audio · media session");LinearLayout panel=a.column();panel.setGravity(Gravity.CENTER);mapHost.addView(panel,new FrameLayout.LayoutParams(-1,-1));largeMedia=new ControlMedia(a,null);largeMedia.title=text("Not Playing",24);largeMedia.artist=text("",16);largeMedia.status=text("",13);panel.addView(largeMedia.title);panel.addView(largeMedia.artist);panel.addView(largeMedia.status);LinearLayout row=a.row();panel.addView(row,new LayoutParams(-1,a.dp(64)));final ControlMedia bridge=largeMedia;for(int action:new int[]{6,7,8}){LinearLayout hit=a.column();hit.setGravity(Gravity.CENTER);hit.setFocusable(true);hit.setBackground(buttonBackground());ControlGlyph glyph=new ControlGlyph(a,action);hit.addView(glyph,new LayoutParams(a.dp(28),a.dp(28)));hit.addView(text(action==6?"Previous":action==7?"Play":"Next",12));hit.setOnClickListener(v->bridge.action(action));row.addView(hit,new LayoutParams(0,-1,1));if(action==6)bridge.previous=hit;else if(action==7){bridge.play=hit;bridge.playGlyph=glyph;}else bridge.next=hit;}panel.addView(button("Buka app media",this::chooseApp),new LayoutParams(-1,a.dp(48)));panel.addView(button("Akses media",bridge::explain),new LayoutParams(-1,a.dp(48)));bridge.render();if(active)bridge.resume();}
 }
 void createMap(){
  try{
   org.osmdroid.config.IConfigurationProvider config=org.osmdroid.config.Configuration.getInstance();config.setUserAgentValue(BuildConfig.APPLICATION_ID+"/"+BuildConfig.VERSION_NAME);config.setOsmdroidBasePath(new File(a.getFilesDir(),"maps"));config.setOsmdroidTileCache(new File(a.getCacheDir(),"map-tiles"));config.setTileFileSystemCacheMaxBytes(50L*1024*1024);config.setTileFileSystemCacheTrimBytes(40L*1024*1024);config.setCacheMapTileCount((short)32);
   map=new MapView(a);map.setTileSource(TileSourceFactory.MAPNIK);map.setMultiTouchControls(true);map.getController().setZoom(12.0);map.getController().setCenter(new GeoPoint(3.139,101.6869));
   map.getOverlays().add(new CopyrightOverlay(a));marker=new Marker(map);marker.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_BOTTOM);
   map.setOnTouchListener((v,event)->{if(event.getActionMasked()==android.view.MotionEvent.ACTION_DOWN)follow=false;return false;});mapHost.addView(map,new FrameLayout.LayoutParams(-1,-1));
  }catch(RuntimeException|LinkageError|OutOfMemoryError unavailable){if(map!=null){map.onDetach();map=null;}mapHost.addView(text("Peta tidak tersedia pada peranti ini",16));}
 }
 void stopLocation(){if(locations!=null)try{locations.removeUpdates(position);}catch(RuntimeException ignored){}listening=false;}
 TextView text(String value,int size){TextView v=a.text(value,size,Color.WHITE);v.setMaxLines(1);v.setEllipsize(android.text.TextUtils.TruncateAt.END);return v;}
 android.graphics.drawable.Drawable buttonBackground(){android.graphics.drawable.StateListDrawable states=new android.graphics.drawable.StateListDrawable();android.graphics.drawable.GradientDrawable focus=new android.graphics.drawable.GradientDrawable();focus.setColor(0xaa375f83);focus.setCornerRadius(a.dp(10));focus.setStroke(a.dp(2),Color.WHITE);states.addState(new int[]{android.R.attr.state_focused},focus);states.addState(new int[]{},a.glass(0xff25324d,12));return states;}
 TextView button(String title,Runnable action){TextView v=text(title,14);v.setGravity(Gravity.CENTER);v.setContentDescription(title);v.setFocusable(true);v.setMinHeight(a.dp(48));v.setBackground(buttonBackground());v.setOnClickListener(w->action.run());return v;}
 View icon(String title,String name,Runnable action){FrameLayout v=new FrameLayout(a);v.setContentDescription("Dashboard "+title);v.setTooltipText(title);v.setFocusable(true);v.setBackground(buttonBackground());v.setOnClickListener(w->action.run());android.graphics.Bitmap bitmap=Ios26Icons.named(a,name);View image;if(bitmap!=null){ImageView i=new ImageView(a);i.setImageBitmap(bitmap);image=i;}else image=new DockGlyph(a,DockGlyph.APPS);v.addView(image,new FrameLayout.LayoutParams(a.dp(36),a.dp(36),Gravity.CENTER));return v;}
 LinearLayout card(LinearLayout parent){LinearLayout v=a.column();v.setPadding(a.dp(10),a.dp(8),a.dp(10),a.dp(8));v.setBackground(a.glass(0xff25324d,18));LayoutParams lp=new LayoutParams(-1,-2);lp.bottomMargin=a.dp(8);parent.addView(v,lp);return v;}
 void chooseApp(){java.util.ArrayList<LauncherActivity.App> apps=new java.util.ArrayList<>(a.apps);if(apps.isEmpty())return;String[] names=new String[apps.size()];for(int i=0;i<names.length;i++)names[i]=apps.get(i).label;chooser=new android.app.AlertDialog.Builder(a).setTitle("Buka app media").setItems(names,(d,i)->a.launch(apps.get(i))).setNegativeButton("Tutup",null).show();}
 void launch(String[] preferred){for(String pkg:preferred)for(LauncherActivity.App app:a.apps)if(pkg.equals(DrivePlan.packageOf(app.component))){a.launch(app);return;}android.widget.Toast.makeText(a,"App telefon belum tersedia",android.widget.Toast.LENGTH_SHORT).show();}
 void tick(long now){if(closed)return;String selected=a.prefs.getString("jakim_zone","");if(minute==now/60000&&selected.equals(zone)&&timetable==a.jakimDays&&loaded.equals(a.loadedZone)&&loading==a.jakimLoading)return;minute=now/60000;zone=selected;timetable=a.jakimDays;loaded=a.loadedZone;loading=a.jakimLoading;calendar.setText(DateFormat.getDateInstance(DateFormat.FULL).format(new Date(now)));if(zone.isEmpty()){prayer.setText("Pilih zon JAKIM");prayerDetail.setText("Countdown ikut zon pilihan anda");}else if(!zone.equals(a.loadedZone)||!a.jakimDays.containsKey(JakimCalendar.key(now))){prayer.setText(a.jakimLoading?"Memuat jadual…":"Jadual belum tersedia");prayerDetail.setText("JAKIM · "+zone+" · "+JakimCalendar.key(now));}else{prayer.setText(JakimCalendar.nextPrayer(a.jakimDays,now));prayerDetail.setText("JAKIM · "+zone+" · waktu Malaysia");}}
 boolean permitted(){return a.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED||a.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED;}
 void enableLocation(){if(map==null)return;if(!permitted()){a.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST);return;}a.prefs.edit().putBoolean("dashboard_location",true).apply();follow=true;startLocation();}
 @android.annotation.SuppressLint("MissingPermission") void startLocation(){if(!active||closed||listening||!permitted()||map==null)return;locations=(LocationManager)a.getSystemService(Context.LOCATION_SERVICE);if(locations==null)return;boolean started=false;for(String provider:new String[]{LocationManager.NETWORK_PROVIDER,LocationManager.GPS_PROVIDER})try{if(locations.isProviderEnabled(provider)){locations.requestLocationUpdates(provider,5000,10,position);started=true;}}catch(RuntimeException unavailable){}listening=started;locationStatus.setText(started?"Menunggu lokasi peranti…":"Hidupkan lokasi dalam tetapan Android");}
 void resume(){if(closed||active)return;active=true;media.resume();if(web!=null)web.resume();if(fm!=null)fm.resume();if(largeMedia!=null)largeMedia.resume();if(map!=null)map.onResume();if(a.prefs.getBoolean("dashboard_location",false))startLocation();tick(System.currentTimeMillis());}
 void permissionResult(){if(permitted()){a.prefs.edit().putBoolean("dashboard_location",true).apply();follow=true;startLocation();}else locationStatus.setText("Kebenaran lokasi tidak diberi");}
 void pause(){active=false;media.pause();if(chooser!=null){chooser.dismiss();chooser=null;}stopLocation();if(web!=null)web.pause();if(fm!=null)fm.pause();if(largeMedia!=null)largeMedia.pause();if(map!=null)map.onPause();}
 public void close(){if(closed)return;pause();closed=true;disposeMain();}
}
