package com.shazwan.idash3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Original white glyphs for the dock's Settings and Apps tiles (drawn in a 64-unit box). */
@SuppressLint("ViewConstructor")
final class DockGlyph extends View {
    static final int SETTINGS = 0, APPS = 1;
    private final int kind; private final Paint ink = new Paint(Paint.ANTI_ALIAS_FLAG); private final Path path = new Path();
    DockGlyph(Context c, int kind) { super(c); this.kind = kind; }
    @Override protected void onDraw(Canvas canvas) {
        canvas.save(); canvas.scale(getWidth() / 64f, getHeight() / 64f); ink.setColor(Color.WHITE);
        if (kind == APPS) {
            ink.setStyle(Paint.Style.FILL);
            for (int r = 0; r < 3; r++) for (int col = 0; col < 3; col++) { float x = 17 + col * 11, y = 17 + r * 11; canvas.drawRoundRect(x, y, x + 8, y + 8, 2.4f, 2.4f, ink); }
        } else {
            // Gear: 8 rounded teeth around a ring.
            ink.setStyle(Paint.Style.FILL);
            for (int i = 0; i < 8; i++) { canvas.save(); canvas.rotate(i * 45f, 32, 32); canvas.drawRoundRect(29, 13, 35, 21, 1.5f, 1.5f, ink); canvas.restore(); }
            ink.setStyle(Paint.Style.STROKE); ink.setStrokeWidth(5.5f); canvas.drawCircle(32, 32, 11.5f, ink);
            ink.setStyle(Paint.Style.FILL); canvas.drawCircle(32, 32, 3.2f, ink);
        }
        canvas.restore();
    }
}
