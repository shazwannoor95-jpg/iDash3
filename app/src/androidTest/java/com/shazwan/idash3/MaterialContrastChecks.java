package com.shazwan.idash3;
import android.app.*;import android.os.*;import android.graphics.*;import org.json.*;import java.io.*;
/** Measures the new material's text protection on representative extreme backgrounds. */
public class MaterialContrastChecks extends Instrumentation {
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){Bundle out=new Bundle();int code=Activity.RESULT_CANCELED,count=0;double minimum=100;JSONObject values=new JSONObject();try{
  for(int background:new int[]{Color.WHITE,Color.BLACK,0xff738699})for(int tone:new int[]{1,2})for(int quality:new int[]{0,2})for(int transparency:new int[]{10,45,65}){
   Bitmap bitmap=Bitmap.createBitmap(96,64,Bitmap.Config.ARGB_8888);bitmap.eraseColor(background);GlassBackdrop backdrop=new GlassBackdrop();backdrop.publish(new GlassBackdrop.Frame(bitmap,background,quality));GlassSurface glass=new GlassSurface(1,IosStyle.CC_BASE,20,0,false,backdrop,quality,tone).material(GlassMaterial.IOS26).appearance(new GlassAppearance(tone,transparency,quality,0,1,false,true));double contrast=GlassTextFixture.contrast(getTargetContext(),glass,background,180,100);String key=Integer.toHexString(background)+"-"+tone+"-"+quality+"-"+transparency;values.put(key,contrast);if(contrast<4.5)throw new AssertionError(key+" contrast="+contrast);minimum=Math.min(minimum,contrast);count++;bitmap.recycle();
  }
  try(Writer writer=new FileWriter(new File(getTargetContext().getFilesDir(),"material-contrast.json"))){writer.write(values.toString(2));}out.putString("stream","PASS: "+count+" material text contrast checks; minimum="+minimum+"\n");code=Activity.RESULT_OK;
 }catch(Throwable t){out.putString("stream","FAIL after "+count+": "+t+"\n"+values.toString());}finish(code,out);}
}
