package com.shazwan.idash3;
import static org.junit.Assert.*;
import android.content.res.Configuration;
import org.junit.Test;
public class ReadabilityTest {
 @Test public void defaultPreservesLegacyTone(){for(int tone=0;tone<3;tone++)assertEquals(tone,Readability.tone(0,Configuration.UI_MODE_NIGHT_YES,tone));}
 @Test public void manualChoiceWinsOverSystem(){assertEquals(IosStyle.LIGHT,Readability.tone(1,Configuration.UI_MODE_NIGHT_YES,0));assertEquals(IosStyle.DARK,Readability.tone(2,Configuration.UI_MODE_NIGHT_NO,0));}
 @Test public void systemFollowsNightMaskOnly(){assertEquals(IosStyle.DARK,Readability.tone(3,Configuration.UI_MODE_NIGHT_YES|Configuration.UI_MODE_TYPE_CAR,0));assertEquals(IosStyle.LIGHT,Readability.tone(3,Configuration.UI_MODE_NIGHT_NO|Configuration.UI_MODE_TYPE_CAR,0));}
 @Test public void unknownSystemModePreservesLegacy(){assertEquals(2,Readability.tone(3,Configuration.UI_MODE_NIGHT_UNDEFINED,2));assertEquals(1,Readability.tone(99,Configuration.UI_MODE_NIGHT_YES,1));}
}
