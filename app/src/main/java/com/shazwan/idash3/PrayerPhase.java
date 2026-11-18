package com.shazwan.idash3;

import java.text.SimpleDateFormat;
import java.util.*;

/** Pure MYT event timeline. Syuruk is an additional marker, never a fardu prayer. */
final class PrayerPhase {
 static final TimeZone MY=TimeZone.getTimeZone("Asia/Kuala_Lumpur");
 static final String[] NAMES={"Subuh","Syuruk","Zohor","Asar","Maghrib","Isyak"};
 static final class Event {final int kind;final long at;final String time;Event(int k,long a,String t){kind=k;at=a;time=t;}}
 final Event current,next;
 PrayerPhase(Event c,Event n){current=c;next=n;}
 double progress(long now){return Math.max(0,Math.min(1,(double)(now-current.at)/(next.at-current.at)));}
 String remaining(long now){long m=Math.max(0,(next.at-now+59999)/60000);return m>=60?m/60+"J "+m%60+"M":m+"M";}
 static String key(long now){SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd",Locale.US);f.setTimeZone(MY);return f.format(new Date(now));}
 static TreeMap<Long,Event> timeline(Map<String,String[]> days,long now){
  TreeMap<Long,Event> result=new TreeMap<>();Calendar date=Calendar.getInstance(MY);date.setTimeInMillis(now);date.add(Calendar.DAY_OF_MONTH,-1);
  for(int d=0;d<3;d++,date.add(Calendar.DAY_OF_MONTH,1)){String[] times=days.get(key(date.getTimeInMillis()));if(times==null||times.length!=6)continue;for(int i=0;i<6;i++){Calendar at=(Calendar)date.clone();at.set(Calendar.HOUR_OF_DAY,Integer.parseInt(times[i].substring(0,2)));at.set(Calendar.MINUTE,Integer.parseInt(times[i].substring(3,5)));at.set(Calendar.SECOND,0);at.set(Calendar.MILLISECOND,0);result.put(at.getTimeInMillis(),new Event(i,at.getTimeInMillis(),times[i]));}}
  return result;
 }
 static PrayerPhase at(TreeMap<Long,Event> timeline,long now){Map.Entry<Long,Event> c=timeline.floorEntry(now),n=timeline.higherEntry(now);if(c==null||n==null||n.getKey()-c.getKey()>24*60*60000L)return null;return new PrayerPhase(c.getValue(),n.getValue());}
}
