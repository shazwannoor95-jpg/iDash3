package com.shazwan.idash3;
import org.junit.Test;
import java.io.*;
import static org.junit.Assert.*;
public class CustomWallpaperTest {
 @Test public void boundsAllocationFailureBecomesIOException() throws Exception {
  try { CustomWallpaper.decode(new File("unused"),1280,(path,options)->{throw new OutOfMemoryError("injected");});fail(); }
  catch(IOException expected){assertTrue(expected.getCause() instanceof OutOfMemoryError);}
 }
 @Test public void pixelAllocationFailureBecomesIOException() throws Exception {
  try { CustomWallpaper.decode(new File("unused"),1280,(path,options)->{
   if(options.inJustDecodeBounds){options.outWidth=8000;options.outHeight=6000;return null;}
   assertEquals(8,options.inSampleSize);throw new OutOfMemoryError("injected");
  });fail(); }catch(IOException expected){assertTrue(expected.getCause() instanceof OutOfMemoryError);}
 }
 @Test public void invalidImageDoesNotAttemptPixelDecode() throws Exception {
  int[] calls={0};try{CustomWallpaper.decode(new File("unused"),1280,(path,options)->{calls[0]++;options.outWidth=-1;return null;});fail();}
  catch(IOException expected){assertEquals(1,calls[0]);}
 }
}
