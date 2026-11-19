package com.shazwan.idash3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

/**
 * OPTIONAL API31+ advanced layer: real GPU blur of an image copy of the scenic background.
 * This deliberately DOES NOT blur the whole text/icon View, which would degrade legibility.
 * To enable it, Codex must place it BEHIND the content inside a FrameLayout-based card.
 * It is NOT installed automatically: wrapping old LinearLayouts could alter their layout.
 * Does NOT blur a live map behind a View; the input here is a static bitmap only.
 */
final class UltraGlassBlurPreview extends ImageView {
    UltraGlassBlurPreview(Context c,Bitmap scene,float blurDp,float cornerRadiusDp){
        super(c);
        setImageBitmap(scene);
        setScaleType(ScaleType.CENTER_CROP);
        final float density=getResources().getDisplayMetrics().density;
        setOutlineProvider(new ViewOutlineProvider(){
            @Override public void getOutline(View view,Outline outline){
                outline.setRoundRect(0,0,view.getWidth(),view.getHeight(),cornerRadiusDp*density);
            }
        });
        setClipToOutline(true);
        if(Build.VERSION.SDK_INT>=31){
            float r=Math.max(1f,blurDp*density);
            Api31.apply(this,r);
        }
    }
    @android.annotation.TargetApi(31)
    private static final class Api31 {
        static void apply(View view,float radius){view.setRenderEffect(RenderEffect.createBlurEffect(radius,radius,Shader.TileMode.CLAMP));}
    }
}
