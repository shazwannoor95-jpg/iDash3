package com.shazwan.idash3;
import android.app.*;import android.os.*;import android.graphics.*;import java.io.*;import java.security.*;import org.json.*;
/** Same test binary runs against rc4 and iDash4 to compare the actual Classic renderer pixels. */
public class ClassicRenderProbe extends Instrumentation {
 String tag;
 public void onCreate(Bundle b){super.onCreate(b);tag=b==null?"current":b.getString("tag","current");start();}
 public void onStart(){Bundle result=new Bundle();int code=Activity.RESULT_CANCELED;try{
  JSONObject profiles=new JSONObject();Bitmap source=Bitmap.createBitmap(96,64,Bitmap.Config.ARGB_8888);int[] colors=new int[96*64];for(int y=0;y<64;y++)for(int x=0;x<96;x++)colors[y*96+x]=Color.rgb(x*255/95,y*255/63,(x+y)*255/158);source.setPixels(colors,0,96,0,0,96,64);
  for(int style=0;style<3;style++)for(int q=0;q<3;q++)for(int tone=0;tone<3;tone++)for(int custom=0;custom<2;custom++){
   GlassBackdrop backdrop=new GlassBackdrop();backdrop.publish(new GlassBackdrop.Frame(source,0xff476a83,q));GlassSurface surface=new GlassSurface(1,0xff456789,22,style,false,backdrop,q,tone);if(custom==1)surface.appearance(new GlassAppearance(tone,45,q,0,1,true,true));surface.setBounds(0,0,180,100);Bitmap image=Bitmap.createBitmap(180,100,Bitmap.Config.ARGB_8888);surface.draw(new Canvas(image));int[] px=new int[180*100];image.getPixels(px,0,180,0,0,180,100);MessageDigest hash=MessageDigest.getInstance("SHA-256");for(int p:px){hash.update((byte)(p>>24));hash.update((byte)(p>>16));hash.update((byte)(p>>8));hash.update((byte)p);}StringBuilder hex=new StringBuilder();for(byte p:hash.digest())hex.append(String.format(java.util.Locale.ROOT,"%02x",p&255));profiles.put(style+"-"+q+"-"+tone+"-"+custom,hex.toString());image.recycle();
  }
  JSONObject report=new JSONObject().put("profiles",profiles).put("count",profiles.length()).put("version",getTargetContext().getPackageManager().getPackageInfo(getTargetContext().getPackageName(),0).versionName);if(!"baseline".equals(tag)){JSONObject expected=new JSONObject(new String(java.nio.file.Files.readAllBytes(new File(getTargetContext().getFilesDir(),"classic-render-baseline.json").toPath()),"UTF-8"));JSONObject old=expected.getJSONObject("profiles");if(!old.toString().equals(profiles.toString())){java.util.Iterator<String> keys=profiles.keys();while(keys.hasNext()){String key=keys.next();if(!profiles.getString(key).equals(old.getString(key)))throw new AssertionError("Classic changed: "+key);}}}
  try(Writer writer=new FileWriter(new File(getTargetContext().getFilesDir(),"classic-render-"+tag+".json"))){writer.write(report.toString(2));}source.recycle();result.putString("stream","PASS: "+profiles.length()+" Classic render profiles ("+tag+")\n");code=Activity.RESULT_OK;
 }catch(Throwable t){result.putString("stream","FAIL: "+t+"\n");}finish(code,result);}
}
