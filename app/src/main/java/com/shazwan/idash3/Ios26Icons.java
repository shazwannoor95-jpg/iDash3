package com.shazwan.idash3;

import android.content.Context;
import android.graphics.*;
import java.io.*;
import java.util.*;

/** Offline iOS26 artwork. Never restyled through the legacy glass icon renderer. */
final class Ios26Icons {
 private static final String ROOT="icons/ios26/";
 static final int CACHE_BYTES=1024*1024;
 private static final android.util.LruCache<String,Bitmap> cache=new android.util.LruCache<String,Bitmap>(CACHE_BYTES){protected int sizeOf(String key,Bitmap bitmap){return bitmap.getByteCount();}};
 private static Map<String,String> components,packages;
 private Ios26Icons(){}
 private static synchronized void index(Context c){
  if(components!=null)return;components=new HashMap<>();packages=new HashMap<>();
  try(BufferedReader reader=new BufferedReader(new InputStreamReader(c.getAssets().open(ROOT+"mapping.tsv"),java.nio.charset.StandardCharsets.UTF_8))){String row;while((row=reader.readLine())!=null){String[] fields=row.split("\t");if(fields.length==3)(fields[0].equals("C")?components:packages).put(fields[1],fields[2]);}}catch(IOException ignored){}
 }
 static synchronized Bitmap named(Context c,String name){
  if(name==null)return null;String key=name.startsWith("ios26_lg_")?name:"ios26_lg_"+name;
  Bitmap hit=cache.get(key);if(hit!=null)return hit;
  try(InputStream input=c.getAssets().open(ROOT+key+".png")){BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=2;options.inPreferredConfig=Bitmap.Config.ARGB_8888;Bitmap image=BitmapFactory.decodeStream(input,null,options);if(image!=null)cache.put(key,image);return image;}catch(IOException|RuntimeException|OutOfMemoryError unavailable){return null;}
 }
 static synchronized String match(Context c,String component,String label){
  index(c);android.content.ComponentName cn=component==null?null:android.content.ComponentName.unflattenFromString(component);
  String pkg=cn==null?"":cn.getPackageName();String exact=cn==null?null:components.get(cn.flattenToString());if(exact!=null)return exact;
  String byPackage=packages.get(pkg);if(byPackage!=null)return byPackage;
  switch(IosGlyphs.match(pkg,label)){
   case IosGlyphs.PHONE:case IosGlyphs.BLUETOOTH:case IosGlyphs.CONTACTS:return "phone";
   case IosGlyphs.MUSIC:return "music";
   case IosGlyphs.PHOTOS:return "photos";
   case IosGlyphs.FILES:case IosGlyphs.DOWNLOAD:return "files";
   case IosGlyphs.SETTINGS:return "settings";
   case IosGlyphs.CALENDAR:return "calendar";
   case IosGlyphs.CLOCK:return "clock";
   case IosGlyphs.BROWSER:return "safari";
   case IosGlyphs.MAPS:return "maps";
   case IosGlyphs.CAMERA:return "camera";
   case IosGlyphs.CALCULATOR:return "calculator";
   case IosGlyphs.MESSAGES:return "messages";
   case IosGlyphs.WEATHER:return "weather";
   case IosGlyphs.YOUTUBE:return "youtube";
   case IosGlyphs.STORE:return "appstore";
   case IosGlyphs.MAIL:return "mail";
   default:return null;
  }
 }
 static Bitmap app(Context c,String component,String label){return named(c,match(c,component,label));}
 static int cacheBytes(){return cache.size();}
 static void clear(){cache.evictAll();} // Shared bitmap references remain safe; never recycle here.
}
