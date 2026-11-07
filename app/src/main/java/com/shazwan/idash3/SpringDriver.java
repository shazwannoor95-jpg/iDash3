package com.shazwan.idash3;

import android.view.Choreographer;

/** Runs springs on vsync only while any of them moves; no work when idle. */
final class SpringDriver implements Choreographer.FrameCallback {
    interface Listener { void onSpringUpdate(); void onSpringRest(); }
    private final Spring[] springs;
    private final Listener listener;
    private long last;
    private boolean running;

    SpringDriver(Listener listener, Spring... springs) { this.listener = listener; this.springs = springs; }

    void start() {
        if (running) return;
        running = true; last = 0;
        Choreographer.getInstance().postFrameCallback(this);
    }

    void cancel() { if (running) { running = false; Choreographer.getInstance().removeFrameCallback(this); } }

    boolean running() { return running; }

    /** Jumps every spring to its target and notifies once (used when motion is disabled). */
    void finish() {
        cancel();
        for (Spring s : springs) s.jumpTo(s.target);
        listener.onSpringUpdate();
        listener.onSpringRest();
    }

    @Override public void doFrame(long frameTimeNanos) {
        if (!running) return;
        float dt = last == 0 ? 1f / 60f : (frameTimeNanos - last) / 1e9f;
        last = frameTimeNanos;
        boolean moving = false;
        for (Spring s : springs) moving |= s.step(dt);
        listener.onSpringUpdate();
        if (moving && running) Choreographer.getInstance().postFrameCallback(this);
        else { running = false; listener.onSpringRest(); }
    }
}
