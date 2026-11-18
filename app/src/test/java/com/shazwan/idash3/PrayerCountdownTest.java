package com.shazwan.idash3;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
public class PrayerCountdownTest {
 String text(long minutes){return new JakimCalendar.NextPrayer("Subuh","05:52",minutes).countdown();}
 @Test public void zero(){assertEquals("0 m",text(0));}
 @Test public void underHour(){assertEquals("59 m",text(59));}
 @Test public void hourBoundary(){assertEquals("1h 0m",text(60));}
 @Test public void fourHoursFortyFive(){assertEquals("4h 45m",text(285));}
 @Test public void reported445Minutes(){assertEquals("7h 25m",text(445));}
}
