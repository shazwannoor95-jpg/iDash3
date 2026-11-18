package com.shazwan.idash3;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/** "Drive" page: pick a map app and a media app, then show them side by side (split-screen). */
final class DrivePage {
    private DrivePage() {}

    static List<String> components(LauncherActivity a) {
        ArrayList<String> out = new ArrayList<>();
        for (LauncherActivity.App app : a.apps) out.add(app.component);
        return out;
    }

    static LauncherActivity.App find(LauncherActivity a, String component) {
        for (LauncherActivity.App app : a.apps) if (app.component.equals(component)) return app;
        return null;
    }

    static void build(final LauncherActivity a, LinearLayout body) {
        final List<String> installed = components(a);
        final String map = DrivePlan.keepOrPick(a.prefs.getString(DriveController.PREF_MAP, null), installed, DrivePlan.MAP_PREFS);
        final String media = DrivePlan.keepOrPick(a.prefs.getString(DriveController.PREF_MEDIA, null), installed, DrivePlan.MEDIA_PREFS);
        final int layout = DriveController.layout(a.prefs);
        final boolean mapLeft = DriveController.mapLeft(a.prefs), bar = a.prefs.getBoolean(DriveController.PREF_BAR, true);

        LinearLayout header = a.row(); LinearLayout words = a.column();
        a.title(words, "Drive", "Split-screen Android · " + (layout == DrivePlan.LAYOUT_NAV_CLOSER ? "navigasi dekat pemandu" : "media dekat pemandu"));
        header.addView(words, new LinearLayout.LayoutParams(0, -2, 1));
        header.addView(a.button("Tutup", () -> { a.page = "home"; a.render(); }));
        header.setContentDescription("Drive page");
        body.addView(header);

        LinearLayout cards = a.row(); cards.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, 0, 1); cp.topMargin = a.dp(4); body.addView(cards, cp);
        View first = card(a, mapLeft ? "PETA" : "MEDIA", mapLeft ? map : media, mapLeft ? DrivePlan.MAP_PREFS : DrivePlan.MEDIA_PREFS, mapLeft ? DriveController.PREF_MAP : DriveController.PREF_MEDIA);
        View second = card(a, mapLeft ? "MEDIA" : "PETA", mapLeft ? media : map, mapLeft ? DrivePlan.MEDIA_PREFS : DrivePlan.MAP_PREFS, mapLeft ? DriveController.PREF_MEDIA : DriveController.PREF_MAP);
        cards.addView(first, new LinearLayout.LayoutParams(0, -1, 1));
        // Same choice as Android Auto's "Change Layout": navigation or media closest to the driver.
        final int flipped = layout == DrivePlan.LAYOUT_NAV_CLOSER ? DrivePlan.LAYOUT_MEDIA_CLOSER : DrivePlan.LAYOUT_NAV_CLOSER;
        TextView swap = a.button("⇄", () -> { a.prefs.edit().putInt(DriveController.PREF_LAYOUT, flipped).remove(DriveController.PREF_MAP_LEFT).apply(); a.render(); });
        swap.setContentDescription(layout == DrivePlan.LAYOUT_NAV_CLOSER ? "Susun atur: navigasi dekat pemandu (tekan untuk media)" : "Susun atur: media dekat pemandu (tekan untuk navigasi)");
        swap.setTextSize(22);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(a.dp(64), a.dp(64)); sp.leftMargin = a.dp(10); sp.rightMargin = a.dp(10); cards.addView(swap, sp);
        cards.addView(second, new LinearLayout.LayoutParams(0, -1, 1));

        // Requirements, each tappable to fix.
        LinearLayout checks = a.row(); checks.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams kp = new LinearLayout.LayoutParams(-1, -2); kp.topMargin = a.dp(6); body.addView(checks, kp);
        boolean accessibility = SplitService.isRunning(), overlay = DriveController.overlayAllowed(a);
        checks.addView(chip(a, "Aksesibiliti (split): " + (accessibility ? "HIDUP" : "MATI — tekan"), accessibility, () -> accessibilityHelp(a)), chipLp(a));
        checks.addView(chip(a, "Bar terapung: " + (!bar ? "OFF" : overlay ? "HIDUP" : "perlu kebenaran — tekan"), bar && overlay, () -> {
            if (!bar) { a.prefs.edit().putBoolean(DriveController.PREF_BAR, true).apply(); a.render(); } else if (!overlay) overlayPermission(a); else { a.prefs.edit().putBoolean(DriveController.PREF_BAR, false).apply(); a.render(); }
        }), chipLp(a));

        LinearLayout actions = a.row(); actions.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(-1, a.dp(60)); ap.topMargin = a.dp(6); body.addView(actions, ap);
        TextView start = a.button(DriveController.isActive() ? "Mulakan semula Drive" : "Mula Drive", () -> {
            String problem = DriveController.start(a, map, media, mapLeft, bar, a.prefs.getBoolean(DriveController.PREF_ADJACENT, false));
            if (problem != null) { Toast.makeText(a, problem, Toast.LENGTH_LONG).show(); if (!SplitService.isRunning()) accessibilityHelp(a); }
            else Toast.makeText(a, "Memulakan Drive… (app pertama → split → app kedua)", Toast.LENGTH_LONG).show();
        });
        start.setTextSize(18); start.setBackground(a.glass(0x9a2f7de1, 26)); start.setContentDescription("Start Drive");
        actions.addView(start, new LinearLayout.LayoutParams(0, -1, 1.6f));
        TextView solo=a.button(a.getString(R.string.map_only),()->{
            if(map==null){Toast.makeText(a,R.string.map_only_missing,Toast.LENGTH_LONG).show();return;}
            if(a.webPanel!=null)a.webPanel.pause();
            if(!DriveController.mapOnly(a,map))Toast.makeText(a,R.string.map_only_missing,Toast.LENGTH_LONG).show();
        });solo.setContentDescription(a.getString(R.string.map_only));actions.addView(solo,new LinearLayout.LayoutParams(0,-1,1));
        if (DriveController.isActive()) {
            TextView stop = a.button("Keluar Drive", () -> { DriveController.stop(a); a.render(); });
            LinearLayout.LayoutParams xp = new LinearLayout.LayoutParams(0, -1, 1); xp.leftMargin = a.dp(10); actions.addView(stop, xp);
        }
    }

    private static LinearLayout.LayoutParams chipLp(LauncherActivity a) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, a.dp(48), 1); p.rightMargin = a.dp(8); return p; }

    private static TextView chip(LauncherActivity a, String text, boolean ok, Runnable action) {
        TextView t = a.text(text, 13, ok ? 0xffb6f2c2 : 0xffffd29a); t.setGravity(Gravity.CENTER); t.setBackground(a.glass(ok ? 0x4034c759 : 0x40ff9f0a, 24));
        t.setPadding(a.dp(10), 0, a.dp(10), 0); t.setMaxLines(2); t.setOnClickListener(v -> action.run()); t.setMinHeight(a.dp(48)); return t;
    }

    private static View card(final LauncherActivity a, String role, String component, final String[] prefs, final String prefKey) {
        LinearLayout card = a.column(); card.setGravity(Gravity.CENTER); card.setPadding(a.dp(12), a.dp(6), a.dp(12), a.dp(6)); card.setBackground(a.glass(0x6a2b3d5c, 26));
        TextView r = a.text(role, 12, 0xffff9f9a); r.setTypeface(IosStyle.font(a, true)); r.setLetterSpacing(.1f); r.setGravity(Gravity.CENTER); r.setTag(GlassSurface.NO_SCRIM); r.setMaxLines(1); r.setIncludeFontPadding(false); card.addView(r);
        LauncherActivity.App app = component == null ? null : find(a, component);
        // The icon absorbs the leftover height (never wider than 64dp), so a short head-unit screen
        // shrinks it instead of clipping the role label above it and the app name below it.
        ImageView icon = new ImageView(a); icon.setScaleType(ImageView.ScaleType.FIT_CENTER); if (app != null) icon.setImageBitmap(app.icon);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(a.dp(64), 0, 1); ip.topMargin = a.dp(2); ip.gravity = Gravity.CENTER_HORIZONTAL; card.addView(icon, ip);
        TextView name = a.text(app == null ? "Belum dipilih" : app.label, 17, Color.WHITE); name.setTypeface(IosStyle.font(a, true)); name.setGravity(Gravity.CENTER); name.setMaxLines(1); name.setEllipsize(android.text.TextUtils.TruncateAt.END); name.setIncludeFontPadding(false);
        card.addView(name, new LinearLayout.LayoutParams(-1, -2));
        final TextView change = a.text("Tekan: tukar · tahan: versi", 12, 0xb3ffffff); change.setGravity(Gravity.CENTER); change.setMaxLines(1); change.setIncludeFontPadding(false);
        card.addView(change, new LinearLayout.LayoutParams(-1, -2));
        // Short cards drop the tap hint rather than letting the three text rows overflow the card.
        final int hintMinHeight = a.dp(120);
        card.addOnLayoutChangeListener((v, l, t, right, bottom, oldL, oldT, oldRight, oldBottom) -> {
            int wanted = bottom - t < hintMinHeight ? View.GONE : View.VISIBLE;
            if (change.getVisibility() != wanted) change.setVisibility(wanted);
        });
        card.setContentDescription(role + ": " + (app == null ? "belum dipilih" : app.label));
        card.setMinimumHeight(a.dp(48));
        card.setOnClickListener(v -> choose(a, role, prefs, prefKey));
        if(app!=null)card.setOnLongClickListener(v->{
            ComponentName componentName=ComponentName.unflattenFromString(app.component);if(componentName==null)return true;
            String version="Tidak diketahui";try{version=a.getPackageManager().getPackageInfo(componentName.getPackageName(),0).versionName;}catch(android.content.pm.PackageManager.NameNotFoundException ignored){}
            new AlertDialog.Builder(a).setTitle(app.label).setMessage("Versi: "+version+"\nAndroid API: "+android.os.Build.VERSION.SDK_INT+"\n\nJika Maps meminta update atau YouTube menunjukkan error 400, semak kemas kini yang serasi dengan peranti. Split-screen tidak membaiki keserasian servis app.")
              .setPositiveButton("App info",(d,w)->{try{a.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+componentName.getPackageName())));}catch(RuntimeException unavailable){Toast.makeText(a,"App info tidak tersedia",Toast.LENGTH_SHORT).show();}}).setNegativeButton("Tutup",null).show();return true;
        });
        return card;
    }

    private static void choose(final LauncherActivity a, String role, String[] prefs, final String prefKey) {
        if (!a.loaded) { Toast.makeText(a, "Senarai app sedang dimuatkan…", Toast.LENGTH_SHORT).show(); return; }
        final ArrayList<LauncherActivity.App> ordered = new ArrayList<>();
        for (String pkg : prefs) for (LauncherActivity.App app : a.apps) if (pkg.equals(DrivePlan.packageOf(app.component)) && !ordered.contains(app)) ordered.add(app);
        int suggested = ordered.size();
        for (LauncherActivity.App app : a.apps) if (!ordered.contains(app)) ordered.add(app);
        String[] labels = new String[ordered.size()];
        for (int i = 0; i < labels.length; i++) labels[i] = (i < suggested ? "★ " : "") + ordered.get(i).label;
        new AlertDialog.Builder(a).setTitle("Pilih app " + role.toLowerCase(java.util.Locale.ROOT)).setItems(labels, (d, w) -> { a.prefs.edit().putString(prefKey, ordered.get(w).component).apply(); a.render(); }).setNegativeButton("Batal", null).show();
    }

    static void accessibilityHelp(final LauncherActivity a) {
        new AlertDialog.Builder(a).setTitle("Hidupkan split-screen iDash4")
            .setMessage("1. Tekan \"Buka tetapan\" → Aksesibiliti → iDash4 Drive → Hidupkan.\n\nPerkhidmatan ini hanya menekan butang split-screen / Back / Home dan tidak membaca skrin.\n\nVia PC (ADB):\nadb shell settings put secure enabled_accessibility_services com.shazwan.idash3/com.shazwan.idash3.SplitService\nadb shell settings put secure accessibility_enabled 1")
            .setPositiveButton("Buka tetapan", (d, w) -> { try { a.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); } catch (RuntimeException e) { Toast.makeText(a, "Tetapan aksesibiliti tidak tersedia pada peranti ini.", Toast.LENGTH_LONG).show(); } })
            .setNegativeButton("Tutup", null).show();
    }

    static void overlayPermission(LauncherActivity a) {
        try { a.startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + a.getPackageName()))); }
        catch (RuntimeException e) { Toast.makeText(a, "Kebenaran \"paparan di atas app lain\" tidak tersedia; Drive berjalan tanpa bar terapung.", Toast.LENGTH_LONG).show(); }
    }
}
