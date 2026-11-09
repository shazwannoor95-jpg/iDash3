package com.shazwan.idash3;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.BatteryManager;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextClock;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/**
 * Android-Auto-style bottom bar shown over the system split-screen apps (needs the "display over
 * other apps" permission): the app launcher, shortcuts to the chosen map/media apps plus phone and
 * Settings, and a status area with battery and clock. There is deliberately no microphone: iDash3
 * never opens a voice assistant. Tapping the launcher opens a grid of installed apps with Exit and
 * a dashboard button that returns to the split-screen.
 */
public class DriveBarService extends Service {
    private static final String CHANNEL = "idash3_drive";
    private static final String[] PHONE_PACKAGES = {"com.google.android.dialer", "com.android.dialer", "com.android.contacts", "com.android.server.telecom", "com.android.phone"};
    private WindowManager windows;
    private View bar, launcher;
    private final List<App> apps = new ArrayList<>();

    /** One launchable app: label, flattened component and its icon. */
    private static final class App {
        final String label, component; final Drawable icon;
        App(String label, String component, Drawable icon) { this.label = label; this.component = component; this.icon = icon; }
    }

    static void start(Context c) {
        try { c.startForegroundService(new Intent(c, DriveBarService.class)); } catch (RuntimeException blocked) { /* background start refused; the bar is optional */ }
    }
    static void stop(Context c) { c.stopService(new Intent(c, DriveBarService.class)); }

    @Override public IBinder onBind(Intent intent) { return null; }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) nm.createNotificationChannel(new NotificationChannel(CHANNEL, "iDash3 Drive", NotificationManager.IMPORTANCE_MIN));
        startForeground(7, new Notification.Builder(this, CHANNEL).setSmallIcon(R.drawable.ic_launcher).setContentTitle("iDash3 Drive").setContentText("Bar Android Auto terapung aktif").build());
        if (bar == null && DriveController.overlayAllowed(this)) addBar();
        return START_NOT_STICKY;
    }

    @Override public void onDestroy() { removeBar(); super.onDestroy(); }

    private void removeBar() {
        hideLauncher();
        if (bar != null && windows != null) { try { windows.removeView(bar); } catch (RuntimeException ignored) { } }
        bar = null;
    }

    private int dp(float v) { return Math.round(v * getResources().getDisplayMetrics().density); }

    private GradientDrawable round(int color, float radiusDp, int stroke) {
        GradientDrawable g = new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp(radiusDp));
        if (stroke != 0) g.setStroke(Math.max(1, dp(1)), stroke);
        return g;
    }

    private void loadApps() {
        apps.clear();
        PackageManager pm = getPackageManager();
        for (ResolveInfo ri : pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)) {
            if (ri.activityInfo == null || getPackageName().equals(ri.activityInfo.packageName)) continue;
            String label = String.valueOf(ri.loadLabel(pm));
            if (TextUtils.isEmpty(label)) label = ri.activityInfo.packageName;
            apps.add(new App(label, new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name).flattenToString(), ri.loadIcon(pm)));
            if (apps.size() >= 60) break;
        }
    }

    private static String packageOf(String component) {
        if (component == null) return "";
        int slash = component.indexOf('/');
        return slash > 0 ? component.substring(0, slash) : component;
    }

    private void addBar() {
        windows = (WindowManager) getSystemService(WINDOW_SERVICE);
        loadApps();
        FrameLayout root = new FrameLayout(this); root.setPadding(dp(10), 0, dp(10), dp(6));
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(6), dp(5), dp(6), dp(5)); row.setBackground(round(0xf2101218, 28, 0x33ffffff));
        row.addView(glyphButton(DockGlyph.APPS, this::toggleLauncher), new LinearLayout.LayoutParams(dp(44), dp(44)));
        row.addView(squareButton(), new LinearLayout.LayoutParams(dp(44), dp(44)));
        addShortcuts(row);
        row.addView(new View(this), new LinearLayout.LayoutParams(dp(10), dp(1)));
        row.addView(status(), new LinearLayout.LayoutParams(-2, -2));
        root.addView(row);
        // Compact centred pill like Android Auto; no FLAG_LAYOUT_NO_LIMITS so it cannot stretch over apps.
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        try { windows.addView(root, lp); bar = root; } catch (RuntimeException denied) { bar = null; stopSelf(); }
    }

    /** Launcher / dashboard / exit button with a 44dp touch target. */
    private View glyphButton(int glyph, Runnable action) {
        FrameLayout holder = new FrameLayout(this);
        holder.setBackground(round(0x1fffffff, 22, 0));
        View icon = new DockGlyph(this, glyph);
        FrameLayout.LayoutParams ip = new FrameLayout.LayoutParams(-1, -1); ip.setMargins(dp(10), dp(10), dp(10), dp(10));
        holder.addView(icon, ip);
        holder.setOnClickListener(v -> action.run());
        return holder;
    }

    /** Android Auto's bottom-left square: one app full screen, tap again to dock the pair back. */
    private View squareButton() {
        FrameLayout holder = new FrameLayout(this);
        holder.setBackground(round(0x1fffffff, 22, 0));
        View icon = new Glyph(this, Glyph.RECENTS);
        FrameLayout.LayoutParams ip = new FrameLayout.LayoutParams(-1, -1); ip.setMargins(dp(13), dp(13), dp(13), dp(13));
        holder.addView(icon, ip);
        holder.setContentDescription("Skrin penuh");
        holder.setOnClickListener(v -> DriveController.toggleFullscreen(this));
        return holder;
    }

    private View status() {
        LinearLayout s = new LinearLayout(this); s.setOrientation(LinearLayout.HORIZONTAL); s.setGravity(Gravity.CENTER_VERTICAL); s.setPadding(dp(8), 0, dp(4), 0);
        s.addView(new Battery(this), new LinearLayout.LayoutParams(dp(20), dp(11)));
        TextClock clock = new TextClock(this); clock.setFormat24Hour("HH:mm"); clock.setFormat12Hour("h:mm");
        clock.setTextColor(Color.WHITE); clock.setTextSize(15); clock.setTypeface(IosStyle.font(this, true));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-2, -2); cp.leftMargin = dp(7); s.addView(clock, cp);
        return s;
    }

    /** App shortcuts in Android Auto order: map, media, phone, Settings (missing apps are skipped). */
    private void addShortcuts(LinearLayout target) {
        SharedPreferences prefs = getSharedPreferences("idash", MODE_PRIVATE);
        addApp(target, prefs.getString(DriveController.PREF_MAP, null));
        addApp(target, prefs.getString(DriveController.PREF_MEDIA, null));
        addApp(target, phoneComponent());
        addApp(target, "com.android.settings/.Settings");
    }

    private String phoneComponent() {
        for (String pkg : PHONE_PACKAGES) for (App app : apps) if (app.component.startsWith(pkg + "/")) return app.component;
        return null;
    }

    private void addApp(LinearLayout target, String component) {
        View view = appIcon(component, 26);
        if (view == null) return;
        view.setOnClickListener(v -> DriveController.launch(this, component, false));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(40), dp(40)); p.leftMargin = dp(2); target.addView(view, p);
    }

    private View appIcon(String component, int sizeDp) {
        ComponentName name = component == null ? null : ComponentName.unflattenFromString(component);
        if (name == null) return null;
        Drawable icon;
        try { icon = getPackageManager().getActivityIcon(name); } catch (PackageManager.NameNotFoundException | RuntimeException missing) { return null; }
        ImageView view = new ImageView(this);
        view.setImageBitmap(IconStyler.render(icon, dp(sizeDp), IconStyler.DEFAULT, 0xff6f8fc8, getResources().getDisplayMetrics().density, false, IosGlyphs.match(name.getPackageName(), name.getPackageName())));
        int pad = dp(sizeDp <= 30 ? 6 : 8); view.setPadding(pad, pad, pad, pad);
        return view;
    }

    private void toggleLauncher() { if (launcher != null) hideLauncher(); else showLauncher(); }

    /**
     * Android-Auto-style app sheet: a decided panel at the bottom over a light scrim, never a
     * full-screen black window. Tapping the scrim or ✕ always closes it, and the panel keeps a
     * visible header even when no app could be listed.
     */
    private void showLauncher() {
        if (windows == null || launcher != null) return;
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0x66000000);
        root.setOnClickListener(v -> hideLauncher());
        LinearLayout panel = new LinearLayout(this); panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackground(round(0xf00b0d12, 24, 0x33ffffff)); panel.setPadding(dp(18), dp(12), dp(18), dp(10));
        panel.setOnClickListener(v -> { });
        LinearLayout header = new LinearLayout(this); header.setOrientation(LinearLayout.HORIZONTAL); header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = new TextView(this); title.setText("Aplikasi"); title.setTextColor(Color.WHITE); title.setTextSize(20); title.setTypeface(IosStyle.font(this, true));
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        header.addView(glyphButton(Glyph.CLOSE, this::hideLauncher), new LinearLayout.LayoutParams(dp(44), dp(44)));
        panel.addView(header);
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(false);
        LinearLayout grid = new LinearLayout(this); grid.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(grid);
        panel.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        fillGrid(grid);
        LinearLayout bottom = new LinearLayout(this); bottom.setOrientation(LinearLayout.HORIZONTAL); bottom.setGravity(Gravity.CENTER_VERTICAL);
        bottom.addView(glyphButton(DockGlyph.DRIVE, this::hideLauncher), new LinearLayout.LayoutParams(dp(44), dp(44)));
        bottom.addView(squareButton(), new LinearLayout.LayoutParams(dp(44), dp(44)));
        addShortcuts(bottom);
        bottom.addView(new View(this), new LinearLayout.LayoutParams(0, -2, 1));
        bottom.addView(status(), new LinearLayout.LayoutParams(-2, -2));
        panel.addView(bottom, new LinearLayout.LayoutParams(-1, -2));
        int panelHeight = Math.round(getResources().getDisplayMetrics().heightPixels * 0.66f);
        FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(-1, panelHeight, Gravity.BOTTOM); pp.setMargins(dp(10), 0, dp(10), dp(10));
        root.addView(panel, pp);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        try { windows.addView(root, lp); launcher = root; } catch (RuntimeException denied) { launcher = null; }
    }

    private void hideLauncher() {
        if (launcher != null && windows != null) { try { windows.removeView(launcher); } catch (RuntimeException ignored) { } }
        launcher = null;
    }

    private void fillGrid(LinearLayout grid) {
        if (apps.isEmpty()) {
            TextView empty = new TextView(this); empty.setText("Tiada app dijumpai. Tutup dan cuba lagi.");
            empty.setTextColor(0xffe6eaff); empty.setTextSize(14); empty.setPadding(dp(6), dp(16), dp(6), dp(4));
            grid.addView(empty, new LinearLayout.LayoutParams(-1, -2));
        }
        final ArrayList<App> cells = new ArrayList<>();
        cells.add(null); // Exit tile
        SharedPreferences prefs = getSharedPreferences("idash", MODE_PRIVATE);
        for (String component : new String[]{prefs.getString(DriveController.PREF_MAP, null), prefs.getString(DriveController.PREF_MEDIA, null)})
            for (App app : apps) if (app.component.equals(component) && !cells.contains(app)) cells.add(app);
        for (App app : apps) if (!cells.contains(app)) cells.add(app);
        LinearLayout row = null;
        for (int i = 0; i < cells.size(); i++) {
            if (i % 4 == 0) { row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER); grid.addView(row, new LinearLayout.LayoutParams(-1, -2)); }
            row.addView(cell(cells.get(i)), new LinearLayout.LayoutParams(0, -2, 1));
        }
    }

    private View cell(final App app) {
        LinearLayout cell = new LinearLayout(this); cell.setOrientation(LinearLayout.VERTICAL); cell.setGravity(Gravity.CENTER); cell.setPadding(dp(6), dp(10), dp(6), dp(10));
        View icon;
        if (app == null) {
            FrameLayout holder = new FrameLayout(this); holder.setBackground(round(0xff1c2028, 28, 0x33ffffff));
            FrameLayout.LayoutParams hp = new FrameLayout.LayoutParams(-1, -1); hp.setMargins(dp(18), dp(18), dp(18), dp(18));
            holder.addView(new Glyph(this, Glyph.CLOSE), hp);
            icon = holder;
            cell.setOnClickListener(v -> DriveController.stop(this));
        } else {
            ImageView view = new ImageView(this);
            Drawable drawable = app.icon;
            view.setImageBitmap(IconStyler.render(drawable, dp(44), IconStyler.DEFAULT, 0xff6f8fc8, getResources().getDisplayMetrics().density, false, IosGlyphs.match(packageOf(app.component), packageOf(app.component))));
            icon = view;
            cell.setOnClickListener(v -> { DriveController.launch(this, app.component, false); hideLauncher(); });
        }
        cell.addView(icon, new LinearLayout.LayoutParams(dp(56), dp(56)));
        TextView label = new TextView(this); label.setText(app == null ? "Exit" : app.label);
        label.setTextColor(0xffe6eaff); label.setTextSize(12); label.setGravity(Gravity.CENTER);
        label.setMaxLines(1); label.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2); lp.topMargin = dp(6); cell.addView(label, lp);
        cell.setContentDescription(app == null ? "Exit Drive" : app.label);
        return cell;
    }

    /** Battery outline + fill from the system percentage (no signal/mic data is read). */
    static final class Battery extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int level;
        Battery(Context c) {
            super(c);
            int value = 50;
            BatteryManager bm = (BatteryManager) c.getSystemService(Context.BATTERY_SERVICE);
            if (bm != null) { int q = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY); if (q >= 0 && q <= 100) value = q; }
            level = value;
        }
        @Override protected void onDraw(Canvas c) {
            float w = getWidth(), h = getHeight();
            p.setColor(0xffe6eaff);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(Math.max(1f, h / 10f));
            c.drawRoundRect(1, 1, w - h * 0.3f, h - 1, h * 0.25f, h * 0.25f, p);
            p.setStyle(Paint.Style.FILL);
            c.drawRoundRect(w - h * 0.2f, h * 0.36f, w - 1, h * 0.64f, 2, 2, p);
            float left = h * 0.22f, full = w - h * 0.4f;
            c.drawRoundRect(left, h * 0.26f, Math.max(left + 1, left + (full - left) * (level / 100f)), h * 0.74f, 2, 2, p);
        }
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
