package com.shazwan.idash3;
import android.app.Activity;
import android.widget.FrameLayout;
/** Shared, reversible FM style selection for Home and Drive Dashboard. */
final class FmPanelHost extends FrameLayout {
 final DashFmPanel classic;final DashFmPanelPremium premium;final DashFmController controller;
 FmPanelHost(Activity owner,float scale,Runnable diagnostics){super(owner);boolean enabled=GlassAppearance.bool(owner.getSharedPreferences("idash",0),"fm_premium",true);classic=enabled?null:new DashFmPanel(owner,scale,diagnostics);premium=enabled?new DashFmPanelPremium(owner,scale,diagnostics):null;controller=enabled?premium.controller:classic.controller;addView(enabled?premium:classic,new LayoutParams(-1,-1));}
 void resume(){if(premium!=null)premium.resume();else classic.resume();}
 void pause(){if(premium!=null)premium.pause();else classic.pause();}
 void close(){if(premium!=null)premium.close();else classic.close();}
 @Override protected void onDetachedFromWindow(){pause();super.onDetachedFromWindow();}
}
