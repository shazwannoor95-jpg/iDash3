package com.shazwan.idash3.debloat;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * One-screen debloat/optimise tool for the Proton Saga head unit. Only DISABLES packages
 * (reversible with "Pulihkan"). With root it applies everything in one tap; without root it walks
 * through each app's settings page so the user taps "Disable", then can uninstall this tool.
 */
public class DebloatActivity extends Activity {
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ArrayList<CheckBox> boxes = new ArrayList<>();
    private TextView status, log;
    private Boolean root;               // null = still checking
    private final ArrayList<String> wizard = new ArrayList<>();
    private int wizardAt = -1;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        build();
        worker.execute(() -> { boolean r = hasRoot(); main.post(() -> { root = r; refresh(); }); });
    }

    @Override protected void onResume() {
        super.onResume();
        refresh();
        if (wizardAt >= 0) main.postDelayed(this::wizardNext, 400);
    }

    @Override protected void onDestroy() { worker.shutdownNow(); super.onDestroy(); }

    private int dp(float v) { return Math.round(v * getResources().getDisplayMetrics().density); }

    private TextView text(String s, float sp, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(sp); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private Button button(String label, int color, View.OnClickListener l) {
        Button btn = new Button(this); btn.setText(label); btn.setAllCaps(false); btn.setTextColor(Color.WHITE); btn.setTextSize(15);
        GradientDrawable g = new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp(14)); btn.setBackground(g);
        btn.setMinHeight(dp(52)); btn.setPadding(dp(14), 0, dp(14), 0); btn.setOnClickListener(l);
        return btn;
    }

    private void build() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.HORIZONTAL);
        root.setBackgroundColor(0xff0f1724); root.setPadding(dp(16), dp(12), dp(16), dp(12));
        // Left: target list.
        LinearLayout left = new LinearLayout(this); left.setOrientation(LinearLayout.VERTICAL);
        left.addView(text("iDash3 Debloat", 24, Color.WHITE, true));
        status = text("Memeriksa…", 13, 0xffb8c4d6, false); left.addView(status);
        ScrollView sc = new ScrollView(this); LinearLayout list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); sc.addView(list);
        for (String[] t : Targets.LIST) {
            CheckBox c = new CheckBox(this); c.setTag(t[0]); c.setChecked("1".equals(t[3])); c.setTextColor(Color.WHITE); c.setTextSize(14);
            c.setMinHeight(dp(52)); boxes.add(c); list.addView(c);
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, 0, 1); lp.topMargin = dp(8);
        left.addView(sc, lp);
        root.addView(left, new LinearLayout.LayoutParams(0, -1, 1.25f));
        // Right: actions + log.
        LinearLayout right = new LinearLayout(this); right.setOrientation(LinearLayout.VERTICAL); right.setPadding(dp(16), 0, 0, 0);
        right.addView(button("Lumpuhkan yang ditanda", 0xff2f7de1, v -> disableSelected()), btnLp());
        right.addView(button("Optimumkan (cache, log, proses)", 0xff2fae60, v -> optimise()), btnLp());
        right.addView(button("Tetapan WebView → Chrome", 0xff5b6676, v -> developerOptions()), btnLp());
        right.addView(button("Pulihkan semua", 0xff8a5a00, v -> restoreAll()), btnLp());
        right.addView(button("Selesai — buang app ini", 0xffc0392b, v -> startActivity(new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + getPackageName())))), btnLp());
        log = text("", 12, 0xffd7e0ee, false); ScrollView ls = new ScrollView(this); ls.addView(log);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(-1, 0, 1); llp.topMargin = dp(8);
        right.addView(ls, llp);
        root.addView(right, new LinearLayout.LayoutParams(0, -1, 1f));
        setContentView(root);
    }

    private LinearLayout.LayoutParams btnLp() { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.bottomMargin = dp(8); return p; }

    private void say(String s) { log.append(s + "\n"); }

    /** -1 not installed, 0 disabled, 1 enabled. */
    private int state(String pkg) {
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(pkg, PackageManager.MATCH_DISABLED_COMPONENTS);
            int s = getPackageManager().getApplicationEnabledSetting(pkg);
            return ai.enabled && s != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER && s != PackageManager.COMPONENT_ENABLED_STATE_DISABLED ? 1 : 0;
        } catch (PackageManager.NameNotFoundException | IllegalArgumentException e) { return -1; }
    }

    private void refresh() {
        int active = 0;
        for (int i = 0; i < boxes.size(); i++) {
            String[] t = Targets.LIST[i]; int s = state(t[0]); CheckBox c = boxes.get(i);
            c.setText(t[1] + "  ·  " + (s < 0 ? "tiada" : s == 0 ? "dilumpuhkan ✓" : "aktif") + "\n" + t[2]);
            c.setEnabled(s >= 0); c.setAlpha(s < 0 ? .45f : 1f);
            if (s == 1) active++;
        }
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE); ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo(); am.getMemoryInfo(mi);
        StatFs data = new StatFs(Environment.getDataDirectory().getPath());
        status.setText(String.format(Locale.ROOT, "Root: %s · RAM %d/%d MB bebas · Storan dalaman bebas %.1f GB · %d sasaran masih aktif · Log diagnostik: %s",
            root == null ? "memeriksa…" : root ? "ADA (automatik)" : "tiada (wizard)", mi.availMem >> 20, mi.totalMem >> 20,
            data.getAvailableBytes() / 1e9, active, human(logBytes())));
    }

    private static String human(long b) { return b < 1 << 20 ? (b >> 10) + " KB" : String.format(Locale.ROOT, "%.1f MB", b / 1048576.0); }

    private long logBytes() { long n = 0; for (String d : Targets.LOG_DIRS) n += size(new File(Environment.getExternalStorageDirectory(), d)); return n; }

    private static long size(File f) { if (!f.exists()) return 0; if (f.isFile()) return f.length(); long n = 0; File[] k = f.listFiles(); if (k != null) for (File c : k) n += size(c); return n; }

    private static boolean delete(File f) { if (f.isDirectory()) { File[] k = f.listFiles(); if (k != null) for (File c : k) delete(c); } return f.delete(); }

    private ArrayList<String> selected(boolean onlyEnabled) {
        ArrayList<String> out = new ArrayList<>();
        for (CheckBox c : boxes) if (c.isChecked() && c.isEnabled()) { String p = (String) c.getTag(); if (!onlyEnabled || state(p) == 1) out.add(p); }
        return out;
    }

    private void disableSelected() {
        if (root == null) { say("Masih memeriksa root, cuba sebentar lagi."); return; }
        ArrayList<String> pkgs = selected(true);
        if (pkgs.isEmpty()) { say("Tiada app aktif yang ditanda."); return; }
        if (root) {
            StringBuilder cmd = new StringBuilder();
            for (String p : pkgs) cmd.append("pm disable-user --user 0 ").append(p).append(";");
            runRoot(cmd.toString(), "Lumpuhkan " + pkgs.size() + " app");
        } else {
            wizard.clear(); wizard.addAll(pkgs); wizardAt = 0;
            new AlertDialog.Builder(this).setTitle("Tiada root — wizard")
                .setMessage("Halaman tetapan setiap app akan dibuka satu demi satu (" + pkgs.size() + " app). Tekan DISABLE (atau MATIKAN), sahkan, kemudian tekan Back. App ini akan buka yang seterusnya.")
                .setPositiveButton("Mula", (d, w) -> wizardNext()).setNegativeButton("Batal", (d, w) -> wizardAt = -1).show();
        }
    }

    private void wizardNext() {
        while (wizardAt >= 0 && wizardAt < wizard.size() && state(wizard.get(wizardAt)) != 1) wizardAt++;
        if (wizardAt < 0) return;
        if (wizardAt >= wizard.size()) { wizardAt = -1; say("Wizard selesai."); refresh(); return; }
        String p = wizard.get(wizardAt);
        say("Langkah " + (wizardAt + 1) + "/" + wizard.size() + ": " + p + " → tekan DISABLE");
        try { startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + p))); }
        catch (RuntimeException e) { say("Tidak dapat membuka tetapan " + p); }
        wizardAt++;
    }

    private void restoreAll() {
        if (root == null) return;
        ArrayList<String> off = new ArrayList<>();
        for (String[] t : Targets.LIST) if (state(t[0]) == 0) off.add(t[0]);
        if (off.isEmpty()) { say("Tiada app yang dilumpuhkan."); return; }
        if (root) { StringBuilder cmd = new StringBuilder(); for (String p : off) cmd.append("pm enable ").append(p).append(";"); runRoot(cmd.toString(), "Pulihkan " + off.size() + " app"); }
        else { wizard.clear(); wizard.addAll(off); say("Tanpa root: buka setiap app dan tekan ENABLE."); for (String p : off) say(" · " + p); wizardAt = -1;
            try { startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + off.get(0)))); } catch (RuntimeException ignored) {} }
    }

    private void optimise() {
        if (root == null) return;
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 7); return; }
        worker.execute(() -> {
            long freed = 0;
            for (String d : Targets.LOG_DIRS) { File f = new File(Environment.getExternalStorageDirectory(), d); long s = size(f); if (s > 0 && delete(f)) freed += s; }
            final long logFreed = freed;
            main.post(() -> say("Log diagnostik dibuang: " + human(logFreed)));
            if (Boolean.TRUE.equals(root)) {
                main.post(() -> runRoot("pm trim-caches 999999M; logcat -c; am kill-all", "Kosongkan cache app, buffer log dan proses latar"));
            } else {
                main.post(() -> { say("Tanpa root: cache app dibersihkan melalui Settings → Storage."); try { startActivity(new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)); } catch (RuntimeException ignored) {} });
            }
        });
    }

    @Override public void onRequestPermissionsResult(int code, String[] p, int[] r) { if (code == 7 && r.length > 0 && r[0] == PackageManager.PERMISSION_GRANTED) optimise(); else if (code == 7) say("Kebenaran storan ditolak; log tidak dibuang."); }

    private void developerOptions() {
        new AlertDialog.Builder(this).setTitle("WebView → Chrome")
            .setMessage("1. Buka Developer options (jika tiada: Settings → About → tekan Build number 7 kali).\n2. Pilih WebView implementation → Chrome.\n\nIni menggantikan WebView 2017 yang lama pada head unit ini.")
            .setPositiveButton("Buka", (d, w) -> { try { startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)); } catch (RuntimeException e) { startActivity(new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS)); } })
            .setNegativeButton("Tutup", null).show();
    }

    private void runRoot(String cmd, String title) {
        say(title + "…");
        worker.execute(() -> {
            String out = exec(cmd);
            main.post(() -> { say(out.isEmpty() ? "Selesai." : out.trim()); refresh(); });
        });
    }

    private static boolean hasRoot() {
        boolean binary = false;
        for (String p : new String[]{"/system/xbin/su", "/system/bin/su", "/sbin/su", "/su/bin/su", "/system/sbin/su", "/vendor/bin/su"}) if (new File(p).exists()) binary = true;
        if (!binary) return false;
        return exec("id").contains("uid=0");
    }

    /** Runs a command through su with a 60 s cap; returns combined output. */
    private static String exec(String cmd) {
        Process p = null;
        try {
            p = new ProcessBuilder("su").redirectErrorStream(true).start();
            try (OutputStream o = p.getOutputStream()) { o.write((cmd + "\nexit\n").getBytes("UTF-8")); }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"))) { String line; while ((line = r.readLine()) != null && sb.length() < 8000) sb.append(line).append('\n'); }
            p.waitFor(60, TimeUnit.SECONDS);
            return sb.toString();
        } catch (Exception e) { return "su gagal: " + e.getMessage(); }
        finally { if (p != null) p.destroy(); }
    }
}
