package com.shazwan.idash3;
import org.junit.Test;import static org.junit.Assert.*;
public class PrayerArtLayoutTest {
 @Test public void sixPhasesUseFiveOriginalScenes(){int[] scenes={0,0,1,2,3,4};for(int i=0;i<6;i++)assertEquals(scenes[i],PrayerArtLayout.scene(i));}
 @Test public void syurukIsMiddleNode(){assertEquals(0,PrayerArtLayout.fraction(0,0),0);assertEquals(.5,PrayerArtLayout.fraction(0,1),0);assertEquals(.5,PrayerArtLayout.fraction(1,0),0);assertEquals(1,PrayerArtLayout.fraction(1,1),0);}
 @Test public void progressIsBoundedAndContinuous(){assertEquals(.25,PrayerArtLayout.fraction(0,.5),0);assertEquals(.75,PrayerArtLayout.fraction(1,.5),0);for(int i=2;i<6;i++){assertEquals(.5,PrayerArtLayout.fraction(i,.5),0);assertEquals(1,PrayerArtLayout.fraction(i,5),0);assertEquals(0,PrayerArtLayout.fraction(i,-1),0);}}
}
