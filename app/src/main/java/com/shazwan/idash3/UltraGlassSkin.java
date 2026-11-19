package com.shazwan.idash3;

import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import java.io.*;
import java.lang.ref.WeakReference;
import java.util.*;

/** Kit skin adapted to current Drive/Apps, preserving geometry and foreground input states. */
final class UltraGlassSkin {
 private static final WeakHashMap<LauncherActivity,Session> active=new WeakHashMap<>();
 private UltraGlassSkin(){}
 static void apply(LauncherActivity host,View shell,String page){
  release(host);if(!"apps".equals(page)&&!"drive".equals(page))return;
  boolean scenic="drive".equals(page)?UltraGlassConfig.SCENE_FOR_DRIVE:UltraGlassConfig.SCENE_FOR_APPS;
  shell.setBackground(new UltraGlassScene(scenic?loadArtwork(host):null,scenic));Session s=new Session(host);active.put(host,s);decorate(host,shell,s);s.resume();
 }
 static void afterAppsFiltered(LauncherActivity host,View grid){Session s=active.get(host);if(s!=null&&"apps".equals(host.page))decorate(host,grid,s);}
 static void afterDriveChanged(LauncherActivity host,View view){Session s=active.get(host);if(s!=null&&"drive".equals(host.page))decorate(host,view,s);}
 static void pause(LauncherActivity host){Session s=active.get(host);if(s!=null)s.stop();}
 static void resume(LauncherActivity host){Session s=active.get(host);if(s!=null)s.resume();}
 static void release(LauncherActivity host){Session s=active.remove(host);if(s!=null)s.dispose();}
 static Session session(LauncherActivity host){return active.get(host);}
 private static Bitmap loadArtwork(LauncherActivity host){
  try(InputStream in=host.getAssets().open("ultraglass/mosque_road.webp")){BitmapFactory.Options opts=new BitmapFactory.Options();opts.inScaled=false;opts.inPreferredConfig=Bitmap.Config.ARGB_8888;return BitmapFactory.decodeStream(in,null,opts);}catch(IOException|RuntimeException|OutOfMemoryError unavailable){return null;}
 }
 private static boolean kitRim(Drawable d){if(d instanceof LiquidGlassStrokeDrawable||d instanceof UltraGlassOverlay)return true;return d instanceof InsetDrawable&&kitRim(((InsetDrawable)d).getDrawable());}
 private static void decorate(LauncherActivity host,View view,Session s){
  if(view==null||view instanceof android.webkit.WebView||view instanceof org.osmdroid.views.MapView)return;
  Drawable background=view.getBackground(),material=background;Rect inset=new Rect();
  if(background instanceof InsetDrawable){background.getPadding(inset);material=((InsetDrawable)background).getDrawable();}
  if(material instanceof GlassSurface&&!s.decorated.containsKey(view)){
   Drawable original=view.getForeground();if(kitRim(original))original=null;
   UltraGlassOverlay fx=new UltraGlassOverlay(host.getResources().getDisplayMetrics().density,((GlassSurface)material).radiusDp());Drawable decorated=background instanceof InsetDrawable?new InsetDrawable(fx,inset.left,inset.top,inset.right,inset.bottom):fx;
   // Put original focus/ripple ABOVE decoration. Never stack legacy neon skins.
   if(original!=null)decorated=new LayerDrawable(new Drawable[]{decorated,original});view.setForeground(decorated);
   s.decorated.put(view,new Binding(view,fx,original));
  }
  if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++)decorate(host,group.getChildAt(i),s);}
 }
 static final class Binding {
  final WeakReference<View> view;final WeakReference<UltraGlassOverlay> overlay;final WeakReference<Drawable> original;
  Binding(View v,UltraGlassOverlay fx,Drawable before){view=new WeakReference<>(v);overlay=new WeakReference<>(fx);original=new WeakReference<>(before);}
 }
 static final class Session implements Runnable {
  final WeakReference<LauncherActivity> owner;final WeakHashMap<View,Binding> decorated=new WeakHashMap<>();boolean playing,disposed;long frames;
  Session(LauncherActivity host){owner=new WeakReference<>(host);}
  void resume(){LauncherActivity host=owner.get();if(disposed||host==null||!host.resumed||!host.hasWindowFocus())return;host.main.removeCallbacks(this);playing=true;run();}
  public void run(){LauncherActivity host=owner.get();if(!playing||disposed||host==null)return;if(!host.resumed||!host.hasWindowFocus()||!IosStyle.motion(host)){stop();return;}
   Iterator<Map.Entry<View,Binding>> it=decorated.entrySet().iterator();while(it.hasNext()){
    Binding binding=it.next().getValue();View v=binding.view.get();UltraGlassOverlay fx=binding.overlay.get();if(v==null||fx==null){it.remove();continue;}
    if(v.isAttachedToWindow()&&v.isShown()&&v.getWindowVisibility()==View.VISIBLE&&fx.isVisible()){fx.start();fx.invalidateSelf();}else fx.stop();
   }
   frames++;host.main.postDelayed(this,UltraGlassConfig.FRAME_TIME_MS);
  }
  void stop(){playing=false;LauncherActivity host=owner.get();if(host!=null)host.main.removeCallbacks(this);for(Binding binding:decorated.values()){UltraGlassOverlay fx=binding.overlay.get();if(fx!=null)fx.stop();}}
  void dispose(){stop();disposed=true;for(Binding binding:decorated.values()){View v=binding.view.get();if(v!=null)v.setForeground(binding.original.get());}decorated.clear();}
 }
}
