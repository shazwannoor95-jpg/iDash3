package com.shazwan.idash3;
import android.content.Context;
import android.graphics.*;
import java.io.InputStream;

/** Reference-image clean plates, decoded only on scene changes. No bitmap work in onDraw. */
final class PrayerArt {
 static final String[] FILES={"dawn","noon","afternoon","sunset","night"};
 final Bitmap bitmap;final int index;final float height,nameY,timeY,headingY,countY,footerY,leftX,rightX;
 final Path arc=new Path();final PathMeasure measure=new PathMeasure();final Rect source,icon;
 final RectF bounds,iconBounds;
 PrayerArt(Bitmap image,int i){
  bitmap=image;index=i;height=1000f*image.getHeight()/image.getWidth();source=new Rect(0,0,image.getWidth(),image.getHeight());bounds=new RectF(0,0,1000,height);
  float[][] layout={{.515f,.615f,.568f,.658f,.737f,.143f,.857f,.236f,.765f,.454f,.125f},{.508f,.608f,.505f,.60f,.69f,.145f,.855f,.25f,.747f,.445f,.225f},{.481f,.579f,.49f,.585f,.69f,.145f,.855f,.25f,.741f,.41f,.185f},{.515f,.62f,.535f,.625f,.725f,.15f,.85f,.274f,.74f,.46f,.20f},{.493f,.565f,.53f,.615f,.70f,.147f,.857f,.26f,.74f,.459f,.20f}};
  float[] v=layout[i];nameY=v[0]*height;timeY=v[1]*height;headingY=v[2]*height;countY=v[3]*height;footerY=v[4]*height;leftX=v[5]*1000;rightX=v[6]*1000;
  arc.moveTo(v[7]*1000,v[9]*height);arc.quadTo(500,v[10]*height,v[8]*1000,v[9]*height);measure.setPath(arc,false);
  float l=.05f,t=i==4?.22f:.16f,r=.25f,b=.44f;icon=new Rect(Math.round(l*image.getWidth()),Math.round(t*image.getHeight()),Math.round(r*image.getWidth()),Math.round(b*image.getHeight()));iconBounds=new RectF(l*1000,t*height,r*1000,b*height);
 }
 static PrayerArt load(Context c,int index){
  try{String path="prayer_art/"+FILES[index]+".webp";BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;try(InputStream in=c.getAssets().open(path)){BitmapFactory.decodeStream(in,null,o);}o.inSampleSize=1;while(o.outWidth/o.inSampleSize>900)o.inSampleSize*=2;o.inJustDecodeBounds=false;o.inPreferredConfig=Bitmap.Config.ARGB_8888;try(InputStream in=c.getAssets().open(path)){Bitmap b=BitmapFactory.decodeStream(in,null,o);return b==null?null:new PrayerArt(b,index);}}
  catch(java.io.IOException|RuntimeException|OutOfMemoryError unavailable){return null;}
 }
}
