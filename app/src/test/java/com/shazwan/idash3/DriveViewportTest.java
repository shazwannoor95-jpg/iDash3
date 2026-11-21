package com.shazwan.idash3;
import org.junit.Test;import static org.junit.Assert.*;
public class DriveViewportTest {
 @Test public void rejectsCorruptSavedCamera(){assertNull(DriveViewport.of(Double.NaN,0,12));assertNull(DriveViewport.of(0,Double.POSITIVE_INFINITY,12));assertNull(DriveViewport.of(0,0,Double.NaN));assertNull(DriveViewport.of(91,0,12));assertNull(DriveViewport.of(0,-181,12));assertNull(DriveViewport.of(0,0,23));}
 @Test public void preservesValidWorldEdgesAndFractionalZoom(){DriveViewport value=DriveViewport.of(-90,180,14.5);assertNotNull(value);assertEquals(-90,value.latitude,0);assertEquals(180,value.longitude,0);assertEquals(14.5,value.zoom,0);assertNotNull(DriveViewport.of(90,-180,0));}
}
