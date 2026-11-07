package com.shazwan.idash3;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

/** Slight spring "give" when a control is pressed. Never consumes the touch, so click/long-press still work. */
final class Elastic implements View.OnTouchListener, SpringDriver.Listener {
    private final View view;
    private final Spring scale = new Spring(700f, .55f, 1f);
    private final SpringDriver driver = new SpringDriver(this, scale);

    private Elastic(View v) { view = v; }

    @SuppressLint("ClickableViewAccessibility")
    static void attach(View v) {
        if (GlassPerf.low()) return;
        v.setOnTouchListener(new Elastic(v));
    }

    @Override public boolean onTouch(View v, MotionEvent e) {
        int action = e.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) scale.target = .93f;
        else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) scale.target = 1f;
        else return false;
        if (!IosStyle.motion(v.getContext())) { scale.jumpTo(scale.target); onSpringUpdate(); return false; }
        driver.start();
        return false;
    }

    @Override public void onSpringUpdate() { view.setScaleX(scale.value); view.setScaleY(scale.value); }
    @Override public void onSpringRest() { onSpringUpdate(); }
}
