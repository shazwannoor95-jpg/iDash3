package com.shazwan.idash3;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.RectF;
import android.text.InputType;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Right-hand home area: paged grid of installed apps and simple folders with page dots.
 * Swipe pages snap with a spring. Long-press opens a car-safe action list (no drag gestures):
 * move earlier/later, add to folder, assign to dock, remove from folder.
 */
@SuppressLint("ViewConstructor")
final class HomeGrid extends LinearLayout implements SpringDriver.Listener {
    static final String PREF = "home_layout";
    private final LauncherActivity a;
    private final FrameLayout pager;
    private final LinearLayout dots;
    private final Spring offset = new Spring(420f, .9f, 0f);
    private final SpringDriver driver = new SpringDriver(this, offset);
    private final ArrayList<LinearLayout> pages = new ArrayList<>();
    private HomeLayout layout = new HomeLayout();
    private final Map<String, LauncherActivity.App> byComponent = new HashMap<>();
    private int columns = 1, rows = 1, perPage = 1, lastW, lastH;
    private float downX, downY, startOffset; private boolean dragging; private VelocityTracker velocity; private final int slop;
    private FrameLayout folderLayer;

    HomeGrid(LauncherActivity a) {
        super(a);
        this.a = a;
        setOrientation(VERTICAL);
        slop = ViewConfiguration.get(a).getScaledTouchSlop();
        pager = new FrameLayout(a) {
            @Override public boolean onInterceptTouchEvent(MotionEvent e) { return intercept(e); }
            @SuppressLint("ClickableViewAccessibility") @Override public boolean onTouchEvent(MotionEvent e) { return drag(e); }
        };
        pager.setClipChildren(true);
        addView(pager, new LayoutParams(-1, 0, 1));
        dots = new LinearLayout(a);
        dots.setGravity(Gravity.CENTER);
        addView(dots, new LayoutParams(-1, a.dp(28)));
        setContentDescription("Home apps");
        offset.jumpTo(a.homePage);
    }

    /** Called with the freshly loaded app list (already label-sorted). */
    void setApps(List<LauncherActivity.App> apps) {
        byComponent.clear();
        ArrayList<String> installed = new ArrayList<>();
        for (LauncherActivity.App app : apps) { byComponent.put(app.component, app); installed.add(app.component); }
        layout = HomeLayout.parse(a.prefs.getString(PREF, ""));
        if (layout.sync(installed)) save();
        rebuild();
    }

    private void save() { a.prefs.edit().putString(PREF, layout.serialize()).apply(); }

    @Override protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        if (w != lastW || h != lastH) { lastW = w; lastH = h; post(this::rebuild); }
    }

    private void rebuild() {
        int w = pager.getWidth(), h = pager.getHeight();
        if (w <= 0 || h <= 0) return;
        // Cell: >=48dp touch target; 76x88dp gives a 52dp icon plus a readable label.
        columns = Math.max(2, Math.min(7, w / a.dp(76)));
        rows = Math.max(1, Math.min(5, h / a.dp(88)));
        perPage = columns * rows;
        int count = layout.items.size(), total = HomeLayout.pages(count, perPage);
        pager.removeAllViews(); pages.clear();
        for (int p = 0; p < total; p++) {
            LinearLayout page = new LinearLayout(a); page.setOrientation(VERTICAL);
            for (int r = 0; r < rows; r++) {
                LinearLayout line = new LinearLayout(a); line.setOrientation(HORIZONTAL);
                for (int c = 0; c < columns; c++) {
                    int index = p * perPage + r * columns + c;
                    View cell = index < count ? cell(index) : new View(a);
                    line.addView(cell, new LayoutParams(0, -1, 1));
                }
                page.addView(line, new LayoutParams(-1, 0, 1));
            }
            pager.addView(page, new FrameLayout.LayoutParams(-1, -1));
            pages.add(page);
        }
        float max = Math.max(0, total - 1);
        if (offset.target > max) { offset.target = max; offset.jumpTo(max); }
        renderDots(total);
        onSpringUpdate();
    }

    private View cell(int index) {
        HomeLayout.Item item = layout.items.get(index);
        LinearLayout tile = new LinearLayout(a); tile.setOrientation(VERTICAL); tile.setGravity(Gravity.CENTER);
        View icon;
        String label;
        if (item.isFolder()) {
            label = item.name;
            GridLayout mini = new GridLayout(a); mini.setColumnCount(2); mini.setPadding(a.dp(6), a.dp(6), a.dp(6), a.dp(6));
            mini.setBackground(a.glass(0x6a2b3d5c, 16));
            for (int i = 0; i < Math.min(4, item.children.size()); i++) {
                LauncherActivity.App app = byComponent.get(item.children.get(i));
                ImageView m = new ImageView(a); if (app != null) m.setImageBitmap(app.icon);
                GridLayout.LayoutParams lp = new GridLayout.LayoutParams(); lp.width = a.dp(19); lp.height = a.dp(19); lp.setMargins(a.dp(1), a.dp(1), a.dp(1), a.dp(1));
                mini.addView(m, lp);
            }
            icon = mini;
            tile.setContentDescription("Folder " + item.name + ", " + item.children.size() + " apps");
            tile.setOnClickListener(v -> openFolder(v, index));
        } else {
            LauncherActivity.App app = byComponent.get(item.component);
            label = app == null ? "" : app.label;
            ImageView image = new ImageView(a); if (app != null) image.setImageBitmap(app.icon);
            icon = image;
            tile.setContentDescription("Open " + label);
            tile.setOnClickListener(v -> { if (app != null) a.launch(app); });
        }
        tile.addView(icon, new LayoutParams(a.dp(52), a.dp(52)));
        TextView name = a.text(label, 12, Color.WHITE); name.setMaxLines(1); name.setEllipsize(TextUtils.TruncateAt.END); name.setGravity(Gravity.CENTER);
        name.setShadowLayer(a.dp(2), 0, a.dp(1), 0x88000000);
        LayoutParams np = new LayoutParams(-1, a.dp(22)); np.topMargin = a.dp(4); tile.addView(name, np);
        tile.setMinimumHeight(a.dp(48)); tile.setMinimumWidth(a.dp(48));
        tile.setOnLongClickListener(v -> { actions(index); return true; });
        Elastic.attach(tile);
        return tile;
    }

    private void renderDots(int total) {
        dots.removeAllViews();
        if (total <= 1) return;
        int current = Math.round(offset.target);
        for (int i = 0; i < total; i++) {
            final int page = i;
            View dot = new View(a);
            android.graphics.drawable.GradientDrawable d = new android.graphics.drawable.GradientDrawable();
            d.setShape(android.graphics.drawable.GradientDrawable.OVAL); d.setColor(i == current ? 0xf0ffffff : 0x66ffffff);
            dot.setBackground(d);
            FrameLayout hit = new FrameLayout(a); // 28dp tall row; each dot gets a 40dp-wide hit area
            hit.setContentDescription("Page " + (i + 1) + " of " + total);
            hit.setOnClickListener(v -> goTo(page));
            FrameLayout.LayoutParams dp = new FrameLayout.LayoutParams(a.dp(8), a.dp(8), Gravity.CENTER);
            hit.addView(dot, dp);
            dots.addView(hit, new LayoutParams(a.dp(40), -1));
        }
    }

    void goTo(int page) {
        int max = Math.max(0, pages.size() - 1);
        offset.target = Math.max(0, Math.min(max, page));
        a.homePage = Math.round(offset.target);
        renderDots(pages.size());
        if (!IosStyle.motion(a)) driver.finish(); else driver.start();
    }

    @Override public void onSpringUpdate() {
        int w = pager.getWidth();
        for (int i = 0; i < pages.size(); i++) {
            float x = (i - offset.value) * w;
            View p = pages.get(i);
            p.setTranslationX(x);
            p.setVisibility(Math.abs(i - offset.value) < 1.02f ? VISIBLE : INVISIBLE);
        }
    }

    @Override public void onSpringRest() { onSpringUpdate(); }

    private boolean intercept(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: downX = e.getX(); downY = e.getY(); dragging = false; track(e); return false;
            case MotionEvent.ACTION_MOVE:
                track(e);
                if (pages.size() > 1 && Math.abs(e.getX() - downX) > slop && Math.abs(e.getX() - downX) > Math.abs(e.getY() - downY) * 1.3f) {
                    dragging = true; startOffset = offset.value; driver.cancel(); downX = e.getX();
                    if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                return false;
            default: return false;
        }
    }

    private void track(MotionEvent e) { if (velocity == null) velocity = VelocityTracker.obtain(); velocity.addMovement(e); }

    private boolean drag(MotionEvent e) {
        if (!dragging) return e.getActionMasked() == MotionEvent.ACTION_DOWN && pages.size() > 1;
        track(e);
        int w = Math.max(1, pager.getWidth());
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                float max = Math.max(0, pages.size() - 1), raw = startOffset - (e.getX() - downX) / w;
                // Rubber band beyond the first/last page.
                if (raw < 0) raw = raw * .3f; else if (raw > max) raw = max + (raw - max) * .3f;
                offset.jumpTo(raw); onSpringUpdate();
                return true;
            case MotionEvent.ACTION_UP: case MotionEvent.ACTION_CANCEL:
                velocity.computeCurrentVelocity(1000);
                float vx = velocity.getXVelocity();
                velocity.recycle(); velocity = null; dragging = false;
                int target = Math.round(offset.value);
                if (Math.abs(vx) > a.dp(400)) target = vx < 0 ? (int) Math.floor(offset.value) + 1 : (int) Math.ceil(offset.value) - 1;
                offset.velocity = -vx / w;
                goTo(target);
                return true;
        }
        return true;
    }

    private void actions(int index) {
        HomeLayout.Item item = layout.items.get(index);
        ArrayList<String> labels = new ArrayList<>(); ArrayList<Runnable> run = new ArrayList<>();
        if (index > 0) { labels.add("Alih ke kiri"); run.add(() -> { layout.move(index, -1); commit(); }); }
        if (index < layout.items.size() - 1) { labels.add("Alih ke kanan"); run.add(() -> { layout.move(index, 1); commit(); }); }
        if (index > 0) { labels.add("Alih ke permulaan"); run.add(() -> { layout.move(index, -index); commit(); }); }
        if (item.isFolder()) {
            labels.add("Namakan semula folder"); run.add(() -> askName(item.name, name -> { layout.rename(index, name); commit(); }));
        } else {
            labels.add("Masukkan ke folder…"); run.add(() -> chooseFolder(index));
            LauncherActivity.App app = byComponent.get(item.component);
            if (app != null) { labels.add("Tetapkan ke dock…"); run.add(() -> new AlertDialog.Builder(a).setTitle("Dock slot").setItems(a.slots, (d, w) -> a.assign(w, app)).show()); }
        }
        new AlertDialog.Builder(a).setTitle(item.isFolder() ? item.name : labelOf(item.component))
            .setItems(labels.toArray(new String[0]), (d, w) -> run.get(w).run()).setNegativeButton("Batal", null).show();
    }

    private String labelOf(String component) { LauncherActivity.App app = byComponent.get(component); return app == null ? "App" : app.label; }

    private void chooseFolder(int index) {
        List<String> names = layout.folderNames();
        String[] options = new String[names.size() + 1];
        options[0] = "Folder baharu…";
        for (int i = 0; i < names.size(); i++) options[i + 1] = names.get(i);
        new AlertDialog.Builder(a).setTitle("Masukkan ke folder").setItems(options, (d, w) -> {
            if (w == 0) askName("Folder", name -> { layout.addToFolder(index, name); commit(); });
            else { layout.addToFolder(index, names.get(w - 1)); commit(); }
        }).setNegativeButton("Batal", null).show();
    }

    interface NameCallback { void accept(String name); }

    private void askName(String initial, NameCallback cb) {
        EditText input = new EditText(a); input.setSingleLine(); input.setInputType(InputType.TYPE_CLASS_TEXT); input.setText(initial); input.setSelectAllOnFocus(true);
        new AlertDialog.Builder(a).setTitle("Nama folder").setView(input).setPositiveButton("OK", (d, w) -> cb.accept(input.getText().toString())).setNegativeButton("Batal", null).show();
    }

    private void commit() { save(); rebuild(); }

    // ---- Folder panel (liquid morph open/close) ----

    boolean folderOpen() { return folderLayer != null; }

    private void openFolder(View source, int index) {
        if (folderLayer != null) return;
        HomeLayout.Item folder = layout.items.get(index);
        FrameLayout stage = a.stage;
        RectF from = rectIn(source, stage);
        int cols = Math.min(4, Math.max(2, folder.children.size())), lines = (folder.children.size() + cols - 1) / cols;
        float pw = Math.min(stage.getWidth() - a.dp(48), cols * a.dp(96) + a.dp(32)), ph = Math.min(stage.getHeight() - a.dp(48), lines * a.dp(96) + a.dp(76));
        RectF to = new RectF((stage.getWidth() - pw) / 2f, (stage.getHeight() - ph) / 2f, (stage.getWidth() + pw) / 2f, (stage.getHeight() + ph) / 2f);

        folderLayer = new FrameLayout(a);
        folderLayer.setBackgroundColor(0x55000000);
        folderLayer.setOnClickListener(v -> closeFolder());
        folderLayer.setContentDescription("Close folder");
        LinearLayout panel = a.column(); panel.setPadding(a.dp(16), a.dp(12), a.dp(16), a.dp(12));
        panel.setBackground(a.glass(0x9a2b3d5c, 28)); panel.setClickable(true);
        TextView title = a.text(folder.name, 20, Color.WHITE); title.setTypeface(IosStyle.font(a, true)); title.setGravity(Gravity.CENTER);
        panel.addView(title, new LayoutParams(-1, a.dp(44)));
        GridLayout g = new GridLayout(a); g.setColumnCount(cols);
        for (String component : new ArrayList<>(folder.children)) {
            LauncherActivity.App app = byComponent.get(component);
            LinearLayout t = a.column(); t.setGravity(Gravity.CENTER);
            ImageView im = new ImageView(a); if (app != null) im.setImageBitmap(app.icon); t.addView(im, new LayoutParams(a.dp(52), a.dp(52)));
            TextView n = a.text(app == null ? "" : app.label, 12, Color.WHITE); n.setMaxLines(1); n.setEllipsize(TextUtils.TruncateAt.END); n.setGravity(Gravity.CENTER);
            t.addView(n, new LayoutParams(-1, a.dp(22)));
            t.setContentDescription("Open " + (app == null ? "" : app.label));
            t.setOnClickListener(v -> { closeFolderNow(); if (app != null) a.launch(app); });
            t.setOnLongClickListener(v -> {
                new AlertDialog.Builder(a).setTitle(app == null ? "App" : app.label).setItems(new String[]{"Keluarkan dari folder"}, (d, w) -> {
                    int fi = layout.items.indexOf(folder); closeFolderNow(); layout.removeFromFolder(fi, component); commit();
                }).setNegativeButton("Batal", null).show();
                return true;
            });
            Elastic.attach(t);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(); lp.width = a.dp(96); lp.height = a.dp(92);
            g.addView(t, lp);
        }
        ScrollView sc = new ScrollView(a); sc.addView(g);
        LinearLayout.LayoutParams gp = new LayoutParams(-2, 0, 1); gp.gravity = Gravity.CENTER_HORIZONTAL;
        panel.addView(sc, gp);
        FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(Math.round(to.width()), Math.round(to.height()));
        pp.leftMargin = Math.round(to.left); pp.topMargin = Math.round(to.top);
        folderLayer.addView(panel, pp);
        panel.setVisibility(INVISIBLE);
        stage.addView(folderLayer, new FrameLayout.LayoutParams(-1, -1));
        final FrameLayout layer = folderLayer;
        MorphOverlay morph = new MorphOverlay(a, from, to, a.dp(16), a.dp(28), true, () -> { if (folderLayer == layer) panel.setVisibility(VISIBLE); });
        stage.addView(morph, new FrameLayout.LayoutParams(-1, -1));
        morph.start();
        folderSource = source; folderRect = to;
    }

    private View folderSource; private RectF folderRect;

    /** Animated close (Back key or scrim tap). Returns false if nothing was open. */
    boolean closeFolder() {
        if (folderLayer == null) return false;
        final FrameLayout layer = folderLayer; folderLayer = null;
        FrameLayout stage = a.stage;
        RectF icon = folderSource != null && folderSource.isAttachedToWindow() ? rectIn(folderSource, stage) : null;
        stage.removeView(layer);
        if (icon != null && folderRect != null) {
            MorphOverlay morph = new MorphOverlay(a, icon, folderRect, a.dp(16), a.dp(28), false, null);
            stage.addView(morph, new FrameLayout.LayoutParams(-1, -1));
            morph.start();
        }
        return true;
    }

    void closeFolderNow() { if (folderLayer != null) { a.stage.removeView(folderLayer); folderLayer = null; } }

    private static RectF rectIn(View v, View ancestor) {
        int[] a = new int[2], b = new int[2];
        v.getLocationInWindow(a); ancestor.getLocationInWindow(b);
        float l = a[0] - b[0], t = a[1] - b[1];
        return new RectF(l, t, l + v.getWidth(), t + v.getHeight());
    }
}
