package com.shazwan.idash3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Optional premium FM skin for iDash3. Native Android Views, API 26+, no libraries.
 * UI only: all OEM FM commands remain inside the existing DashFmController.
 *
 * Integration (a local experiment; keep the original DashFmPanel.java):
 * 1. In LauncherActivity, change `DashFmPanel fmPanel` to
 *    `DashFmPanelPremium fmPanel`.
 * 2. Change `new DashFmPanel(this,...` to `new DashFmPanelPremium(this,...`.
 * 3. Do not change DashFmController/DashFmBand/OEM intents.
 */
final class DashFmPanelPremium extends LinearLayout {
    private static final int INK = 0xFFF9F7FF;
    private static final int MUTED = 0xFFCAC3DD;
    private static final int ACCENT = 0xFFCD87FF;
    private static final int STROKE = 0x887E6F9E;

    final DashFmController controller;
    private final Activity owner;
    private final float density;
    private final Runnable diagnostics;
    private final TextView frequency, status, source, power;
    private final TextView[] presets = new TextView[10];
    private final LinearLayout[] presetRows = new LinearLayout[2];
    private final DialScale dial;
    private AlertDialog dialog;
    private boolean active, closed, compact;

    DashFmPanelPremium(Activity owner, float scale, Runnable diagnostics) {
        super(owner);
        this.owner = owner;
        this.density = Math.max(0.5f, scale);
        this.diagnostics = diagnostics;
        setOrientation(VERTICAL);
        setPadding(dp(8), dp(8), dp(8), dp(8));
        setContentDescription("Premium FM panel");
        setBackground(new android.graphics.drawable.LayerDrawable(new android.graphics.drawable.Drawable[]{panelBackground(),new PremiumWave()}));

        // Controller is unchanged: firmware selection, lifecycle, safety and tuning
        // are delegated to the project's existing implementation.
        controller = new DashFmController(owner, this::refresh);

        LinearLayout header = row();
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = text("FM RADIO", 15, ACCENT, true);
        title.setCompoundDrawablesWithIntrinsicBounds(new Glyph(2),null,null,null);title.setCompoundDrawablePadding(dp(6));title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        header.addView(title, new LayoutParams(0, LayoutParams.MATCH_PARENT, 1));

        LinearLayout actions=row();actions.setGravity(Gravity.CENTER_VERTICAL);actions.setContentDescription("FM toolbar");
        power = iconButton(0, "Start or stop the OEM FM service", () -> {if(controller.owned)controller.stop();else controller.start();});
        actions.addView(power,fixed(48,48,0));
        TextView tune=iconButton(1,"Enter FM frequency",this::editFrequency);actions.addView(tune,fixed(48,48,6));
        source=button("Auto ▾","Choose FM source",this::chooseSource,false);source.setSingleLine(true);source.setEllipsize(TextUtils.TruncateAt.END);actions.addView(source,fixed(88,48,6));
        TextView menu=iconButton(3,"FM settings, stations, presets and diagnostics",this::options);actions.addView(menu,fixed(48,48,6));
        header.addView(actions,new LayoutParams(LayoutParams.WRAP_CONTENT,dp(48)));
        addView(header, new LayoutParams(LayoutParams.MATCH_PARENT, dp(48)));

        status = text("", 12, MUTED, false);
        status.setSingleLine(true);
        status.setEllipsize(TextUtils.TruncateAt.END);
        status.setGravity(Gravity.CENTER_VERTICAL);
        addView(status, new LayoutParams(LayoutParams.MATCH_PARENT, dp(28)));

        LinearLayout tuner = row();
        tuner.setGravity(Gravity.CENTER_VERTICAL);
        TextView minus = button("−", "Tune down 0.10 MHz", () ->
                controller.frequency(DashFmBand.step(controller.frequency, -1)), false);
        minus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        tuner.addView(minus, fixed(52, -1, 0));

        LinearLayout tunerCenter = row();
        tunerCenter.setOrientation(VERTICAL);
        tunerCenter.setGravity(Gravity.CENTER);
        frequency = text("", 50, INK, true);
        frequency.setGravity(Gravity.CENTER);
        frequency.setIncludeFontPadding(false);
        frequency.setSingleLine(true);
        // API 26 native auto-size supports both 1024x600 and 800x480.
        frequency.setAutoSizeTextTypeUniformWithConfiguration(
                22, 50, 2, TypedValue.COMPLEX_UNIT_SP);
        frequency.setContentDescription("Current FM frequency. Tap to enter frequency");
        frequency.setOnClickListener(v -> { if (canAct()) editFrequency(); });
        tunerCenter.addView(frequency, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));
        dial = new DialScale(owner);
        tunerCenter.addView(dial, new LayoutParams(LayoutParams.MATCH_PARENT, dp(25)));
        tuner.addView(tunerCenter, new LayoutParams(0, LayoutParams.MATCH_PARENT, 1));

        TextView plus = button("+", "Tune up 0.10 MHz", () ->
                controller.frequency(DashFmBand.step(controller.frequency, 1)), false);
        plus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        tuner.addView(plus, fixed(52, -1, 0));
        addView(tuner, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));

        for (int r = 0; r < 2; r++) {
            LinearLayout presetRow = row();
            presetRows[r] = presetRow;
            for (int c = 0; c < 5; c++) {
                final int slot = r * 5 + c;
                TextView preset = button("", "Preset " + (slot + 1) + "; hold to save", () -> {
                    int saved = getPreset(slot);
                    if (DashFmBand.valid(saved)) controller.frequency(saved);
                    else Toast.makeText(owner, "Hold to save this frequency", Toast.LENGTH_SHORT).show();
                }, false);
                preset.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                preset.setLineSpacing(0, 0.92f);
                preset.setOnLongClickListener(v -> {
                    if (!canAct()) return true;
                    savePreset(slot);
                    Toast.makeText(owner, "Preset " + (slot + 1) + " saved", Toast.LENGTH_SHORT).show();
                    return true;
                });
                presets[slot] = preset;
                LayoutParams pp = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
                pp.leftMargin=c==0?0:dp(6);
                presetRow.addView(preset, pp);
            }
            LayoutParams rp=new LayoutParams(LayoutParams.MATCH_PARENT,dp(52));rp.topMargin=dp(6);addView(presetRow,rp);
        }
        refresh();
    }

    private int dp(float n) { return Math.max(1, Math.round(n * density)); }
    private boolean canAct() { return active && !closed; }
    private LayoutParams fixed(int w, int h, int leftMargin) {
        LayoutParams p = new LayoutParams(dp(w), h < 0 ? LayoutParams.MATCH_PARENT : dp(h));
        p.leftMargin = dp(leftMargin);
        return p;
    }
    private LinearLayout row() {
        LinearLayout l = new LinearLayout(owner);
        l.setOrientation(HORIZONTAL);
        return l;
    }
    private TextView text(String value, int sizeSp, int color, boolean bold) {
        TextView t = new TextView(owner);
        t.setText(value);
        t.setTextColor(color);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setTypeface(IosStyle.font(owner, bold));
        return t;
    }
    private GradientDrawable panelBackground() {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{0xE71A1532, 0xE7261B46, 0xEF151629});
        d.setCornerRadius(dp(23));
        d.setStroke(dp(1), 0x998E78B0);
        return d;
    }
    private GradientDrawable baseButtonBackground(boolean selected) {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                selected ? new int[]{0xF07F3FB7, 0xD34B2C83} :
                        new int[]{0xC44B405F, 0xB827263F});
        d.setCornerRadius(dp(15));
        d.setStroke(dp(1), selected ? 0xFFDC9FFF : STROKE);
        return d;
    }
    private android.graphics.drawable.Drawable buttonBackground(boolean selected){
        android.graphics.drawable.StateListDrawable states=new android.graphics.drawable.StateListDrawable();
        GradientDrawable focus=baseButtonBackground(true);focus.setStroke(dp(3),INK);
        states.addState(new int[]{android.R.attr.state_focused},focus);states.addState(new int[]{android.R.attr.state_pressed},baseButtonBackground(true));states.addState(new int[]{},baseButtonBackground(selected));return states;
    }
    private TextView button(String value, String description, Runnable action, boolean emphasized) {
        TextView t = text(value, 13, INK, emphasized);
        t.setGravity(Gravity.CENTER);
        t.setBackground(buttonBackground(false));
        t.setContentDescription(description);t.setTooltipText(description);t.setFocusable(true);
        t.setOnClickListener(v -> { if (canAct()) action.run(); });
        return t;
    }
    private TextView iconButton(int type,String description,Runnable action){
        IconControl v=new IconControl(type);v.setBackground(buttonBackground(false));v.setContentDescription(description);v.setTooltipText(description);v.setFocusable(true);v.setOnClickListener(w->{if(canAct())action.run();});return v;
    }
    final class IconControl extends TextView {
        final Glyph glyph;
        IconControl(int type){super(owner);glyph=new Glyph(type);setText("");setGravity(Gravity.CENTER);setIncludeFontPadding(false);}
        @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){super.onSizeChanged(w,h,oldw,oldh);int side=dp(24);glyph.setBounds((w-side)/2,(h-side)/2,(w+side)/2,(h+side)/2);}
        android.graphics.Rect iconBounds(){return glyph.getBounds();}
        @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);glyph.draw(canvas);}
    }
    private int getPreset(int slot) {
        try {
            return owner.getSharedPreferences(DashFmController.PREFS, Context.MODE_PRIVATE)
                    .getInt("preset_" + slot, 0);
        } catch (ClassCastException invalid) { return 0; }
    }
    private void savePreset(int slot) {
        owner.getSharedPreferences(DashFmController.PREFS, Context.MODE_PRIVATE)
                .edit().putInt("preset_" + slot, controller.frequency).apply();
        refresh();
    }
    private void refresh() {
        if (closed) return;
        String mhz = DashFmBand.text(controller.frequency);
        SpannableString tuned = new SpannableString(mhz + " MHz");
        int from = mhz.length();
        tuned.setSpan(new RelativeSizeSpan(0.49f), from, tuned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tuned.setSpan(new ForegroundColorSpan(MUTED), from, tuned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        frequency.setText(tuned);
        status.setText(controller.message);
        status.setContentDescription(controller.message);
        source.setText(controller.sourceLabel() + " ▾");
        power.setText("");power.setContentDescription(controller.owned?"Stop FM":"Start FM");power.setTooltipText(controller.owned?"Stop FM":"Start FM");power.invalidate();
        power.setBackground(buttonBackground(controller.owned));
        dial.invalidate();
        for (int i = 0; i < presets.length; i++) {
            int khz = getPreset(i);
            String suffix = DashFmBand.valid(khz) ? DashFmBand.text(khz) : "—";
            presets[i].setText((i + 1) + "\n" + suffix);
            boolean selected = DashFmBand.valid(khz) && khz == controller.frequency;
            presets[i].setSelected(selected);presets[i].setContentDescription("FM channel "+(i+1)+"; "+suffix+"; hold to save");presets[i].setBackground(buttonBackground(selected));
        }
    }

    private void show(AlertDialog shown) {
        if (dialog != null) dialog.dismiss();
        dialog = shown;
        shown.setOnDismissListener(d -> { if (dialog == shown) dialog = null; });
        shown.show();
    }
    private void chooseSource() {
        String[] values = {"auto", "internal", "external"};
        String[] labels = {"Auto · use head-unit selection", "Built-in FM · MediaTek", "External FM · GalaRadio"};
        int selected = "internal".equals(controller.sourceMode) ? 1 :
                "external".equals(controller.sourceMode) ? 2 : 0;
        show(new AlertDialog.Builder(owner)
                .setTitle("FM source")
                .setSingleChoiceItems(labels, selected, (d, index) -> {
                    controller.source(values[index]); d.dismiss();
                }).setNegativeButton("Cancel", null).create());
    }
    private void editFrequency() {
        EditText input = new EditText(owner);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(DashFmBand.text(controller.frequency));
        input.selectAll();
        input.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(8)});
        LinearLayout body = new LinearLayout(owner);
        body.setOrientation(VERTICAL);
        body.setPadding(dp(16), dp(5), dp(16), dp(5));
        body.addView(input, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        TextView error = text("", 12, 0xFFFFB4AB, false);
        error.setVisibility(GONE);
        body.addView(error, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        AlertDialog editor = new AlertDialog.Builder(owner)
                .setTitle("Tune FM · MHz")
                .setView(body)
                .setPositiveButton("Tune", null)
                .setNegativeButton("Cancel", null)
                .create();
        show(editor);
        editor.setOnDismissListener(d->{android.view.inputmethod.InputMethodManager keyboard=(android.view.inputmethod.InputMethodManager)owner.getSystemService(Context.INPUT_METHOD_SERVICE);if(keyboard!=null&&input.getWindowToken()!=null)keyboard.hideSoftInputFromWindow(input.getWindowToken(),0);input.clearFocus();if(dialog==editor)dialog=null;});
        editor.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            try {
                int khz = DashFmBand.parse(input.getText().toString());
                controller.frequency(khz);
                editor.dismiss();
            } catch (IllegalArgumentException invalid) {
                error.setText(invalid.getMessage());
                error.setVisibility(VISIBLE);
            }
        });
    }
    private void options() {
        show(new AlertDialog.Builder(owner).setTitle("FM options")
                .setItems(new String[]{"Available stations", "Select saved preset", "Save current frequency", "Radio report"},
                        (d, which) -> {
                            if (which == 0) chooseStation();
                            else if (which == 1) presetList(false);
                            else if (which == 2) presetList(true);
                            else diagnostics.run();
                        })
                .setNegativeButton("Close", null).create());
    }
    private void chooseStation() {
        int[] items = controller.stations.clone();
        if (items.length == 0) {
            Toast.makeText(owner, "No stations reported by FM service", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] labels = new String[items.length];
        for (int i = 0; i < items.length; i++) labels[i] = DashFmBand.text(items[i]) + " MHz";
        show(new AlertDialog.Builder(owner).setTitle("FM stations")
                .setItems(labels, (d, i) -> controller.frequency(items[i]))
                .setNegativeButton("Cancel", null).create());
    }
    private void presetList(boolean save) {
        String[] labels = new String[presets.length];
        for (int i = 0; i < labels.length; i++) {
            int khz = getPreset(i);
            labels[i] = "P" + (i + 1) + " · " +
                    (save ? "Save " + DashFmBand.text(controller.frequency) + " MHz" :
                            DashFmBand.valid(khz) ? DashFmBand.text(khz) + " MHz" : "Empty");
        }
        show(new AlertDialog.Builder(owner)
                .setTitle(save ? "Save FM preset" : "FM presets")
                .setItems(labels, (d, index) -> {
                    int khz = getPreset(index);
                    if (save || !DashFmBand.valid(khz)) savePreset(index);
                    else controller.frequency(khz);
                })
                .setNegativeButton("Cancel", null).create());
    }
    void resume() { if (!closed) active = true; }
    void pause() {
        active = false;
        if (dialog != null) { dialog.dismiss(); dialog = null; }
        controller.stop();
    }
    void close() {
        if (closed) return;
        pause();
        controller.close();
        closed = true;
    }
    @Override protected void onMeasure(int w, int h) {
        int height = MeasureSpec.getSize(h);
        boolean small = MeasureSpec.getMode(h) != MeasureSpec.UNSPECIFIED && height < dp(315);
        if (small != compact) {
            compact = small;
            status.getLayoutParams().height = dp(small ? 23 : 28);
            for (LinearLayout row : presetRows) row.getLayoutParams().height = dp(52);
        }
        super.onMeasure(w, h);
    }

    /** Cached decorative wave and halo; no bitmap, blur or animation loop. */
    private final class PremiumWave extends android.graphics.drawable.Drawable {
        final Paint ink=new Paint(3);final android.graphics.Path wave=new android.graphics.Path(),edge=new android.graphics.Path();android.graphics.Shader halo,shade;
        protected void onBoundsChange(android.graphics.Rect b){float w=b.width(),h=b.height();if(w<=0||h<=0)return;halo=new android.graphics.RadialGradient(w*.5f,h*.38f,Math.max(1,w*.6f),new int[]{0x557f3dd2,0x00161029},null,android.graphics.Shader.TileMode.CLAMP);shade=new android.graphics.LinearGradient(0,h*.3f,0,h,new int[]{0x443d2169,0x0a9b4cf0},null,android.graphics.Shader.TileMode.CLAMP);wave.reset();wave.moveTo(0,h*.54f);wave.cubicTo(w*.24f,h*.20f,w*.66f,h*.21f,w,h*.54f);edge.set(wave);wave.lineTo(w,h);wave.lineTo(0,h);wave.close();}
        public void draw(Canvas c){if(halo==null)return;ink.setStyle(Paint.Style.FILL);ink.setShader(halo);c.drawRect(getBounds(),ink);ink.setShader(shade);c.drawPath(wave,ink);ink.setShader(null);ink.setColor(0x668b54bc);ink.setStrokeWidth(dp(1));ink.setStyle(Paint.Style.STROKE);c.drawPath(edge,ink);}
        public void setAlpha(int a){ink.setAlpha(a);}public void setColorFilter(android.graphics.ColorFilter f){ink.setColorFilter(f);}public int getOpacity(){return android.graphics.PixelFormat.TRANSLUCENT;}
    }

    private final class Glyph extends android.graphics.drawable.Drawable {
        final Paint ink=new Paint(3);final int type;
        Glyph(int type){this.type=type;setBounds(0,0,dp(24),dp(24));}
        public int getIntrinsicWidth(){return dp(24);}public int getIntrinsicHeight(){return dp(24);}
        public void draw(Canvas c){int save=c.save();android.graphics.Rect b=getBounds();c.translate(b.left,b.top);c.scale(b.width()/24f,b.height()/24f);ink.setColor(type==2?ACCENT:INK);ink.setStyle(Paint.Style.STROKE);ink.setStrokeWidth(1.8f);ink.setStrokeCap(Paint.Cap.ROUND);
            if(type==0){if(controller.owned){ink.setStyle(Paint.Style.FILL);c.drawRoundRect(7,7,17,17,2,2,ink);}else{c.drawArc(4,4,20,20,-45,270,false,ink);c.drawLine(12,2,12,12,ink);}}
            else if(type==1){for(int y=6;y<=18;y+=6){c.drawLine(3,y,21,y,ink);c.drawCircle(y==12?15:8,y,2.5f,ink);}}
            else if(type==3){ink.setStyle(Paint.Style.FILL);for(int x=5;x<=19;x+=7)c.drawCircle(x,12,1.5f,ink);}
            else {c.drawCircle(12,7,2,ink);c.drawLine(12,9,7,22,ink);c.drawLine(12,9,17,22,ink);c.drawLine(9,17,15,17,ink);c.drawArc(4,0,20,16,135,90,false,ink);c.drawArc(0,-4,24,20,135,90,false,ink);c.drawArc(4,0,20,16,-45,90,false,ink);c.drawArc(0,-4,24,20,-45,90,false,ink);}c.restoreToCount(save);
        }
        public void setAlpha(int a){ink.setAlpha(a);}public void setColorFilter(android.graphics.ColorFilter f){ink.setColorFilter(f);}public int getOpacity(){return android.graphics.PixelFormat.TRANSLUCENT;}
    }

    /** Static tick ruler; one cheap Canvas pass, no animated blur or continuous invalidation. */
    private final class DialScale extends View {
        private final Paint ticks = new Paint(Paint.ANTI_ALIAS_FLAG);
        DialScale(Context c) { super(c); setContentDescription("Frequency tuner scale"); }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float width = getWidth(), height = getHeight();
            float middle = width / 2f, usable = Math.max(0, width - dp(12));
            float spacing = usable / 40f;
            ticks.setStrokeWidth(dp(1));
            for (int i = -20; i <= 20; i++) {
                float x = middle + i * spacing;
                boolean major = (i % 5 == 0);
                ticks.setColor(major ? 0xAAAF8BE2 : 0x667A6C9F);
                canvas.drawLine(x, height - dp(3), x, height - dp(major ? 15 : 9), ticks);
            }
            ticks.setColor(ACCENT);
            ticks.setStrokeWidth(dp(2));
            canvas.drawLine(middle, dp(1), middle, height - dp(1), ticks);
        }
    }
}
