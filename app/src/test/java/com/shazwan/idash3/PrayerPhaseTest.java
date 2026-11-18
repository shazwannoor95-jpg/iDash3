package com.shazwan.idash3;
import org.junit.Test;import static org.junit.Assert.*;import java.util.*;import java.text.*;
public class PrayerPhaseTest {
 long at(String date,String time)throws Exception{SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);f.setTimeZone(PrayerPhase.MY);return f.parse(date+" "+time).getTime();}
 Map<String,String[]> days(){Map<String,String[]> d=new HashMap<>();for(String key:new String[]{"2026-12-31","2027-01-01","2027-01-02"})d.put(key,new String[]{"05:52","07:10","13:08","16:36","19:23","20:15"});return d;}
 PrayerPhase phase(Map<String,String[]> d,long n){return PrayerPhase.at(PrayerPhase.timeline(d,n),n);}
 @Test public void everyExactBoundaryAndSecondBefore()throws Exception{String[] times={"05:52","07:10","13:08","16:36","19:23","20:15"};for(int i=0;i<6;i++){long n=at("2027-01-01",times[i]+":00");assertEquals(i,phase(days(),n).current.kind);assertEquals((i+5)%6,phase(days(),n-1).current.kind);assertEquals(0,phase(days(),n).progress(n),0);assertEquals((i+1)%6,phase(days(),n).next.kind);}}
 @Test public void midnightAndYearDoNotResetIsyak()throws Exception{long n=at("2027-01-01","00:00:00");PrayerPhase p=phase(days(),n);assertEquals(5,p.current.kind);assertEquals(0,p.next.kind);assertEquals("5J 52M",p.remaining(n));assertTrue(p.progress(n)>0&&p.progress(n)<1);}
 @Test public void syurukHasItsOwnNonPrayerPhase()throws Exception{assertEquals(1,phase(days(),at("2027-01-01","08:00:00")).current.kind);assertEquals(2,phase(days(),at("2027-01-01","08:00:00")).next.kind);}
 @Test public void countdownRoundsUpAndProgressIsTimeBased()throws Exception{long n=at("2027-01-01","06:31:00");PrayerPhase p=phase(days(),n);assertEquals(.5,p.progress(n),.0001);assertEquals("39M",p.remaining(n));assertEquals("1M",p.remaining(p.next.at-1));}
 @Test public void missingPreviousAndNextNeverInventTimes()throws Exception{Map<String,String[]> d=days();d.remove("2026-12-31");assertNull(phase(d,at("2027-01-01","02:00:00")));d.remove("2027-01-02");assertNull(phase(d,at("2027-01-01","23:00:00")));}
 @Test public void leapDay()throws Exception{Map<String,String[]> d=new HashMap<>();d.put("2028-02-29",days().get("2027-01-01"));d.put("2028-03-01",days().get("2027-01-01"));long n=at("2028-03-01","00:00:00");assertEquals(5,phase(d,n).current.kind);assertEquals(0,phase(d,n).next.kind);}
}
