package com.shazwan.idash3;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
public class ContentInsetsTest {
 @Test public void hiddenNavigationDoesNotReserveStaleRightBar(){assertEquals(0,ContentInsets.remaining(48,0,0,true));}
 @Test public void visibleOverlappingNavigationIsReserved(){assertEquals(48,ContentInsets.remaining(48,0,0,false));}
 @Test public void decorAlreadyReservedNavigation(){assertEquals(0,ContentInsets.remaining(48,0,48,false));}
 @Test public void onlyRemainingOverlapIsReserved(){assertEquals(28,ContentInsets.remaining(48,0,20,false));}
 @Test public void cutoutStaysProtectedInImmersiveMode(){assertEquals(30,ContentInsets.remaining(48,30,0,true));}
 @Test public void reservedCutoutIsNotCountedTwice(){assertEquals(0,ContentInsets.remaining(48,30,30,true));}
}
