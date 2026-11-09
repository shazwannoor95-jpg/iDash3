package com.shazwan.idash3;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;

/**
 * Floating control pill shown over the split-screen apps (needs the "display over other apps"
 * permission): clock, Back / Home / Recents, volume, shortcuts to the map and media apps, and exit.
 */
public class DriveBarService extends Service {
    private static final String CHANNEL = "idash3_drive";
    private WindowManager windows;
    private View bar;

    static void start(Context c) {
        try { c.startForegroundService(new Intent(c, DriveBarService.class)); } catch (RuntimeException blocked) { /* background start refused; the bar is optional */ }
    }
    static void stop(Context c) { c.stopService(new Intent(c, DriveBarService.class)); }

    @Override public IBinder onBind(Intent intent) { return null; }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) nm.createNotificationChannel(new NotificationChannel(CHANNEL, "iDash3 Drive", NotificationManager.IMPORTANCE_MIN));
        startForeground(7, new Notification.Builder(this, CHANNEL).setSmallIcon(R.drawable.ic_launcher).setContentTitle("iDash3 Drive").setContentText("Bar kawalan terapung aktif").build());
        if (bar == null && DriveController.overlayAllowed(this)) addBar();
        return START_NOT_STICKY;
    }

    @Override public void onDestroy() { removeBar(); super.onDestroy(); }

    private void removeBar() { if (bar != null && windows != null) { try { windows.removeView(bar); } catch (RuntimeException ignored) { } } bar = null; }

    private int dp(float v) { return Math.round(v * getResources().getDisplayMetrics().density); }

    private void addBar() {
        windows = (WindowManager) getSystemService(WINDOW_SERVICE);
        final float density = getResources().getDisplayMetrics().density;
        LinearLayout pill = new LinearLayout(this);
        pill.setOrientation(LinearLayout.HORIZONTAL); pill.setGravity(Gravity.CENTER_VERTICAL); pill.setPadding(dp(9), dp(3), dp(9), dp(3));
        GradientDrawable bg = new GradientDrawable(); bg.setColor(0xe61a2233); bg.setCornerRadius(dp(22)); bg.setStroke(Math.max(1, dp(1)), 0x59ffffff); pill.setBackground(bg);

        TextClock clock = new TextClock(this); clock.setFormat24Hour("HH:mm"); clock.setFormat12Hour("h:mm"); clock.setTextColor(Color.WHITE); clock.setTextSize(15); clock.setTypeface(IosStyle.font(this, true));
        LinearLayout.LayoutParams clockLp = new LinearLayout.LayoutParams(-2, -2); clockLp.leftMargin = dp(2); clockLp.rightMargin = dp(2);
        pill.addView(clock, clockLp);
        boolean controls = SplitService.isRunning();
        if (controls) {
            pill.addView(button(Glyph.BACK, () -> SplitService.back()), cell());
            pill.addView(button(Glyph.HOME, () -> SplitService.home()), cell());
            pill.addView(button(Glyph.RECENTS, () -> SplitService.recents()), cell());
        }
        pill.addView(button(Glyph.VOL_DOWN, () -> volume(AudioManager.ADJUST_LOWER)), cell());
        pill.addView(button(Glyph.VOL_UP, () -> volume(AudioManager.ADJUST_RAISE)), cell());
        android.content.SharedPreferences prefs = getSharedPreferences("idash", MODE_PRIVATE);
        shortcut(pill, prefs.getString(DriveController.PREF_MAP, null));
        shortcut(pill, prefs.getString(DriveController.PREF_MEDIA, null));
        pill.addView(button(Glyph.CLOSE, () -> DriveController.stop(this)), cell());

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL; lp.y = dp(6);
        try { windows.addView(pill, lp); bar = pill; } catch (RuntimeException denied) { bar = null; stopSelf(); }
    }

    private LinearLayout.LayoutParams cell() { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(38), dp(38)); p.leftMargin = dp(2); return p; }

    private void volume(int direction) {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (am != null) am.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI);
    }

    private void shortcut(LinearLayout pill, String component) {
        if (component == null) return;
        try {
            android.content.ComponentName name = android.content.ComponentName.unflattenFromString(component);
            if (name == null) return;
            android.content.pm.PackageManager pm = getPackageManager();
            android.graphics.drawable.Drawable icon = pm.getActivityIcon(name);
            int px = dp(28);
            ImageView view = new ImageView(this);
            view.setImageBitmap(IconStyler.render(icon, px, IconStyler.DEFAULT, 0xff6f8fc8, getResources().getDisplayMetrics().density, false, IosGlyphs.match(name.getPackageName(), name.getPackageName())));
            view.setOnClickListener(v -> DriveController.launch(this, component, false));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(38), dp(38)); p.leftMargin = dp(2); view.setPadding(dp(5), dp(5), dp(5), dp(5));
            pill.addView(view, p);
        } catch (android.content.pm.PackageManager.NameNotFoundException | RuntimeException missing) { /* app removed: no shortcut */ }
    }

    private View button(int glyph, Runnable action) {
        View v = new Glyph(this, glyph);
        v.setOnClickListener(x -> action.run());
        return v;
    }

    /** Minimal vector glyphs so the bar needs no bundled images. */
    static final class Glyph extends View {
        static final int BACK = 0, HOME = 1, RECENTS = 2, VOL_DOWN = 3, VOL_UP = 4, CLOSE = 5;
        private final int kind; private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Glyph(Context c, int kind) { super(c); this.kind = kind; setClickable(true); }
        @Override protected void onDraw(Canvas c) {
            float s = Math.min(getWidth(), getHeight()) / 64f; c.save(); c.translate((getWidth() - 64 * s) / 2f, (getHeight() - 64 * s) / 2f); c.scale(s, s);
            p.setColor(Color.WHITE); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(4.6f); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeJoin(Paint.Join.ROUND);
            android.graphics.Path path = new android.graphics.Path();
            switch (kind) {
                case BACK: path.moveTo(38, 16); path.lineTo(22, 32); path.lineTo(38, 48); c.drawPath(path, p); break;
                case HOME: path.moveTo(14, 32); path.lineTo(32, 16); path.lineTo(50, 32); path.moveTo(20, 30); path.lineTo(20, 48); path.lineTo(44, 48); path.lineTo(44, 30); c.drawPath(path, p); break;
                case RECENTS: c.drawRoundRect(17, 17, 47, 47, 6, 6, p); break;
                case VOL_DOWN: case VOL_UP:
                    p.setStyle(Paint.Style.FILL); path.moveTo(14, 26); path.lineTo(24, 26); path.lineTo(34, 17); path.lineTo(34, 47); path.lineTo(24, 38); path.lineTo(14, 38); path.close(); c.drawPath(path, p);
                    p.setStyle(Paint.Style.STROKE); c.drawLine(40, 32, 52, 32, p); if (kind == VOL_UP) c.drawLine(46, 26, 46, 38, p); break;
                case CLOSE: c.drawLine(20, 20, 44, 44, p); c.drawLine(44, 20, 20, 44, p); break;
                default: break;
            }
            c.restore();
        }
    }
}
