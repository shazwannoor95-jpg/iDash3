package com.shazwan.idash3;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;

/** Only reserve visible, overlapping system bars; immersive bars must not leave a blank strip. */
final class ContentInsets {
    final Activity owner;
    final View root, content;
    final int[] base, bars=new int[4], cutout=new int[4], at=new int[2];
    ContentInsets(Activity owner,View root,View content,int left,int top,int right,int bottom){
        this.owner=owner;this.root=root;this.content=content;base=new int[]{left,top,right,bottom};content.setPadding(left,top,right,bottom);
        root.setOnApplyWindowInsetsListener((v,insets)->{read(insets);apply();root.post(this::apply);return insets;});
        root.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->apply());
        owner.getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(flags->{root.requestApplyInsets();root.post(this::apply);});
        root.requestApplyInsets();
    }
    void read(WindowInsets insets){
        bars[0]=insets.getSystemWindowInsetLeft();bars[1]=insets.getSystemWindowInsetTop();bars[2]=insets.getSystemWindowInsetRight();bars[3]=insets.getSystemWindowInsetBottom();
        java.util.Arrays.fill(cutout,0);
        if(Build.VERSION.SDK_INT>=28&&insets.getDisplayCutout()!=null){android.view.DisplayCutout c=insets.getDisplayCutout();cutout[0]=c.getSafeInsetLeft();cutout[1]=c.getSafeInsetTop();cutout[2]=c.getSafeInsetRight();cutout[3]=c.getSafeInsetBottom();}
    }
    static int remaining(int bar,int cutout,int alreadyReserved,boolean hidden){return Math.max(0,Math.max(hidden?0:bar,cutout)-Math.max(0,alreadyReserved));}
    void apply(){
        if(!root.isLaidOut()||!root.isAttachedToWindow())return;
        View decor=owner.getWindow().getDecorView();root.getLocationInWindow(at);
        int flags=decor.getSystemUiVisibility();boolean navHidden=(flags&View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)!=0,statusHidden=(flags&View.SYSTEM_UI_FLAG_FULLSCREEN)!=0;
        int l=base[0]+remaining(bars[0],cutout[0],at[0],navHidden);
        int t=base[1]+remaining(bars[1],cutout[1],at[1],statusHidden);
        int r=base[2]+remaining(bars[2],cutout[2],decor.getWidth()-at[0]-root.getWidth(),navHidden);
        int b=base[3]+remaining(bars[3],cutout[3],decor.getHeight()-at[1]-root.getHeight(),navHidden);
        if(content.getPaddingLeft()!=l||content.getPaddingTop()!=t||content.getPaddingRight()!=r||content.getPaddingBottom()!=b)content.setPadding(l,t,r,b);
    }
}
