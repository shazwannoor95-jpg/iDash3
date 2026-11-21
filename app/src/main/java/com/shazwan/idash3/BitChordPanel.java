/* SPDX-License-Identifier: GPL-3.0-only. Native host of the vendored BitChord Compose UI. */
package com.shazwan.idash3;
import android.widget.FrameLayout;

final class BitChordPanel extends android.widget.LinearLayout implements AutoCloseable {
 final android.widget.TextView expand;final android.view.View content;boolean closed;
 BitChordPanel(LauncherActivity a){super(a);setOrientation(VERTICAL);setBaselineAligned(false);setContentDescription("BitChord full native music UI");expand=a.text("Besarkan BitChord",12,-1);expand.setGravity(android.view.Gravity.CENTER_VERTICAL|android.view.Gravity.END);expand.setPadding(a.dp(8),0,a.dp(12),0);expand.setBackground(a.glass(0xff25324d,12));expand.setOnClickListener(v->{if(a.driveDashboard!=null)a.driveDashboard.expandMusic();});expand.setContentDescription("Besarkan atau pulihkan BitChord");addView(expand,new LayoutParams(-1,a.dp(48)));content=com.music.bitchord.BitChordEmbedded.create(a);addView(content,new LayoutParams(-1,0,1));}
 // Weighted Android layouts may make an unbounded provisional pass. Compose lists require finite bounds.
 @Override protected void onMeasure(int width,int height){if(MeasureSpec.getMode(width)==MeasureSpec.UNSPECIFIED||MeasureSpec.getMode(height)==MeasureSpec.UNSPECIFIED){setMeasuredDimension(MeasureSpec.getSize(width),MeasureSpec.getSize(height));return;}super.onMeasure(width,height);}
 void resume(){} void pause(){}
 public void close(){closed=true;com.music.bitchord.BitChordEmbedded.dispose(content);removeAllViews();}
}
