package com.shazwan.idash3;
/** The dawn artwork spans Subuh–Zohor, with Syuruk at the middle node. */
final class PrayerArtLayout {
 static int scene(int kind){return kind<2?0:kind-1;}
 static float fraction(int kind,double progress){float p=(float)Math.max(0,Math.min(1,progress));return kind==0?p*.5f:kind==1?.5f+p*.5f:p;}
}
