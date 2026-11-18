package com.shazwan.idash3;
import org.junit.Test;
import static org.junit.Assert.*;
public class VideoFitTest {
 @Test public void matchingAspectNeedsNoCrop(){assertEquals(1f,DashWebPlayer.fillScale(1600,900,16f/9f),.001f);}
 @Test public void widePanelNeedsUniformZoom(){assertEquals(2f,DashWebPlayer.fillScale(1600,450,16f/9f),.001f);}
 @Test public void tallPanelNeedsUniformZoom(){assertEquals(2f,DashWebPlayer.fillScale(800,900,16f/9f),.001f);}
 @Test public void invalidGeometryFallsBack(){assertEquals(1f,DashWebPlayer.fillScale(0,0,0),0);assertEquals(1f,DashWebPlayer.fillScale(800,400,Float.NaN),0);}
 @Test public void extremeCropBounded(){assertEquals(4f,DashWebPlayer.fillScale(8000,100,1),0);}
}
