package com.shazwan.idash3;

import android.app.*;
import android.content.*;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;

/** Actual native dashboard FM UI; the installed OEM service retains tuner/audio access. */
final class DashFmPanel extends LinearLayout {
    final DashFmController controller;
    final TextView frequency,status,power,stations,source;
    private final TextView[] presets=new TextView[10];
    private final Activity owner;
    private final float unit;
    private final Runnable diagnostics;
    private final LinearLayout heading,tuning,favourites;
    private AlertDialog dialog;
    private boolean active,closed,compact;
    DashFmPanel(Activity owner,float scale,Runnable diagnostics){
        super(owner);this.owner=owner;this.unit=Math.max(.1f,scale);this.diagnostics=diagnostics;setOrientation(VERTICAL);setPadding(p(6),p(2),p(6),p(3));
        controller=new DashFmController(owner,this::refresh);
        heading=new LinearLayout(owner);TextView name=label("FM RADIO",12,0xff64c8ff);heading.addView(name,new LayoutParams(0,-1,1));
        power=control("","Start FM",()->{if(controller.owned)controller.stop();else controller.start();});power.setCompoundDrawablesWithIntrinsicBounds(null,new FmIcon(true),null,null);power.setCompoundDrawablePadding(0);heading.addView(power,new LayoutParams(p(48),-1));
        TextView tune=control("","Enter FM frequency",this::editFrequency);tune.setCompoundDrawablesWithIntrinsicBounds(null,new FmIcon(false),null,null);heading.addView(tune,new LayoutParams(p(48),-1));
        source=control("Auto ▾","Choose FM source",this::chooseSource);heading.addView(source,new LayoutParams(p(96),-1));
        heading.addView(control("⋯","FM options",this::options),new LayoutParams(p(48),-1));addView(heading,new LayoutParams(-1,p(48)));
        status=label("",13,0xffc7d8ed);status.setMaxLines(2);addView(status,new LayoutParams(-1,p(38)));
        tuning=new LinearLayout(owner);TextView down=control("−","Tune down 0.1 MHz",()->controller.frequency(DashFmBand.step(controller.frequency,-1)));tuning.addView(down,new LayoutParams(p(48),-1));
        frequency=label("",54,0xffffffff);frequency.setIncludeFontPadding(false);frequency.setGravity(Gravity.CENTER);frequency.setOnClickListener(v->{if(active&&!closed)editFrequency();});frequency.setContentDescription("FM frequency; tap to tune");tuning.addView(frequency,new LayoutParams(0,-1,1));
        tuning.addView(control("+","Tune up 0.1 MHz",()->controller.frequency(DashFmBand.step(controller.frequency,1))),new LayoutParams(p(48),-1));addView(tuning,new LayoutParams(-1,0,1));
        stations=control("Stations","Choose a station returned by the FM service",this::chooseStation);
        favourites=new LinearLayout(owner);favourites.setOrientation(VERTICAL);
        LinearLayout[] rows={new LinearLayout(owner),new LinearLayout(owner)};for(LinearLayout row:rows)favourites.addView(row,new LayoutParams(-1,0,1));
        for(int index=0;index<presets.length;index++){
            final int slot=index;TextView preset=control("", "FM channel "+(slot+1)+"; hold to save",()->{
                int value=preset(slot);if(DashFmBand.valid(value))controller.frequency(value);else Toast.makeText(owner,"Hold to save the selected frequency",Toast.LENGTH_SHORT).show();
            });
            preset.setOnLongClickListener(v->{if(!active||closed)return true;owner.getSharedPreferences(DashFmController.PREFS,Context.MODE_PRIVATE).edit().putInt("preset_"+slot,controller.frequency).apply();refresh();Toast.makeText(owner,"Preset "+(slot+1)+" saved",Toast.LENGTH_SHORT).show();return true;});
            presets[index]=preset;add(rows[index/5],preset);
        }
        addView(favourites,new LayoutParams(-1,p(104)));refresh();
    }
    private int preset(int index){try{return owner.getSharedPreferences(DashFmController.PREFS,Context.MODE_PRIVATE).getInt("preset_"+index,0);}catch(ClassCastException invalid){return 0;}}
    private int p(float n){return Math.max(1,Math.round(n*unit));}
    private TextView label(String value,int size,int color){TextView t=new TextView(owner);t.setTypeface(IosStyle.font(owner,false));t.setText(value);t.setTextColor(color);t.setTextSize(TypedValue.COMPLEX_UNIT_PX,p(size));t.setGravity(Gravity.CENTER_VERTICAL);return t;}
    private TextView control(String value,String description,Runnable action){
        TextView t=label(value,14,0xffffffff);t.setGravity(Gravity.CENTER);t.setContentDescription(description);t.setFocusable(true);
        GradientDrawable b=new GradientDrawable();b.setColor(0x55324461);b.setCornerRadius(p(14));b.setStroke(p(1),0x66d9e7ff);android.graphics.drawable.StateListDrawable states=new android.graphics.drawable.StateListDrawable();GradientDrawable marked=new GradientDrawable();marked.setColor(0x77324461);marked.setCornerRadius(p(14));marked.setStroke(p(3),0xffffffff);states.addState(new int[]{android.R.attr.state_focused},marked);states.addState(new int[]{android.R.attr.state_selected},marked);states.addState(new int[]{},b);t.setBackground(states);t.setOnClickListener(v->{if(!closed&&active)action.run();});return t;
    }
    private void add(LinearLayout row,View child){LayoutParams lp=new LayoutParams(0,-1,1);lp.setMargins(p(3),p(2),p(3),p(2));row.addView(child,lp);}
    private void refresh(){
        // Controller does not notify during construction; all views exist when refresh is first called.
        if(closed)return;
        frequency.setText(DashFmBand.text(controller.frequency)+" MHz");status.setText(controller.message);power.setContentDescription(controller.owned?"Stop FM":"Start FM");power.setTooltipText(controller.owned?"Stop FM":"Start FM");power.setSelected(controller.owned);power.setAlpha(1f);power.invalidate();source.setText(controller.sourceLabel()+" ▾");
        stations.setEnabled(controller.stations.length>0&&!controller.initializing);stations.setAlpha(stations.isEnabled()?1:.4f);
        for(int i=0;i<presets.length;i++){int value=preset(i);presets[i].setText((i+1)+"\n"+(DashFmBand.valid(value)?DashFmBand.text(value):"—"));presets[i].setContentDescription("FM channel "+(i+1)+"; "+(DashFmBand.valid(value)?DashFmBand.text(value)+" MHz":"empty")+"; hold to save");presets[i].setSelected(DashFmBand.valid(value)&&value==controller.frequency);}
    }
    private void show(AlertDialog shown){
        show(shown,()->{});
    }
    private void show(AlertDialog shown,Runnable dismissed){
        if(dialog!=null)dialog.dismiss();dialog=shown;shown.setOnDismissListener(d->{if(dialog==shown)dialog=null;dismissed.run();});shown.show();
    }
    void chooseSource(){
        String[] modes={"auto","internal","external"};String[] names={"Auto · use head-unit FM selection","Built-in FM · MediaTek","External FM · GalaRadio"};
        int selected="internal".equals(controller.sourceMode)?1:"external".equals(controller.sourceMode)?2:0;
        show(new AlertDialog.Builder(owner).setTitle("FM source").setSingleChoiceItems(names,selected,(d,index)->{controller.source(modes[index]);d.dismiss();}).setNegativeButton("Cancel",null).create());
    }
    private void editFrequency(){
        EditText input=new EditText(owner);input.setSingleLine(true);input.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);input.setText(DashFmBand.text(controller.frequency));input.selectAll();
        input.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(8)});
        LinearLayout body=new LinearLayout(owner);body.setOrientation(VERTICAL);body.setPadding(p(16),p(4),p(16),p(4));body.addView(input,new LayoutParams(-1,-2));
        TextView error=new TextView(owner);error.setTextSize(14);error.setTextColor(0xffffb4ab);error.setMaxLines(2);error.setTag("fm-frequency-error");error.setVisibility(GONE);body.addView(error,new LayoutParams(-1,-2));
        Runnable clearInput=()->{error.setText("");error.setVisibility(GONE);android.view.inputmethod.InputMethodManager keyboard=(android.view.inputmethod.InputMethodManager)owner.getSystemService(Context.INPUT_METHOD_SERVICE);if(keyboard!=null&&input.getWindowToken()!=null)keyboard.hideSoftInputFromWindow(input.getWindowToken(),0);input.clearFocus();};
        AlertDialog editor=new AlertDialog.Builder(owner).setTitle("FM frequency · MHz").setView(body).setPositiveButton("Tune",null).setNegativeButton("Cancel",(d,w)->clearInput.run()).create();
        show(editor,clearInput);editor.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{try{int khz=DashFmBand.parse(input.getText().toString());clearInput.run();controller.frequency(khz);editor.dismiss();}catch(IllegalArgumentException invalid){error.setText(invalid.getMessage());error.setVisibility(VISIBLE);}});
    }
    private void chooseStation(){
        int[] values=controller.stations.clone();if(values.length==0)return;String[] names=new String[values.length];for(int i=0;i<values.length;i++)names[i]=DashFmBand.text(values[i])+" MHz";
        show(new AlertDialog.Builder(owner).setTitle("Stations reported by FM").setItems(names,(d,index)->controller.frequency(values[index])).setNegativeButton("Cancel",null).create());
    }
    private void options(){show(new AlertDialog.Builder(owner).setTitle("FM options").setItems(new String[]{"Presets","Save preset","Stations","Radio report"},(d,index)->{if(index<2)presetList(index==1);else if(index==2)chooseStation();else diagnostics.run();}).setNegativeButton("Close",null).create());}
    private void presetList(boolean save){
        String[] names=new String[presets.length];for(int i=0;i<names.length;i++){int value=preset(i);names[i]="P"+(i+1)+" · "+(save?"Save "+DashFmBand.text(controller.frequency):DashFmBand.valid(value)?DashFmBand.text(value)+" MHz":"Empty · save "+DashFmBand.text(controller.frequency));}
        show(new AlertDialog.Builder(owner).setTitle(save?"Save preset":"FM presets").setItems(names,(d,index)->{
            int value=preset(index);if(save||!DashFmBand.valid(value)){owner.getSharedPreferences(DashFmController.PREFS,Context.MODE_PRIVATE).edit().putInt("preset_"+index,controller.frequency).apply();refresh();}else controller.frequency(value);
        }).setNegativeButton("Cancel",null).create());
    }
    void resume(){if(!closed)active=true;}
    void pause(){active=false;if(dialog!=null){dialog.dismiss();dialog=null;}controller.stop();}
    void close(){if(closed)return;pause();controller.close();closed=true;}
    /** Original outline controls matching the existing iOS-inspired control glyphs; no Apple API/assets. */
    private final class FmIcon extends android.graphics.drawable.Drawable {
        final android.graphics.Paint ink=new android.graphics.Paint(3);final boolean power;
        FmIcon(boolean power){this.power=power;setBounds(0,0,p(24),p(24));}
        public int getIntrinsicWidth(){return p(24);}public int getIntrinsicHeight(){return p(24);}
        public void draw(android.graphics.Canvas c){int save=c.save();android.graphics.Rect b=getBounds();c.translate(b.left,b.top);c.scale(b.width()/24f,b.height()/24f);ink.setColor(0xffffffff);ink.setStyle(android.graphics.Paint.Style.STROKE);ink.setStrokeWidth(1.8f);ink.setStrokeCap(android.graphics.Paint.Cap.ROUND);
            if(power&&controller.owned){ink.setStyle(android.graphics.Paint.Style.FILL);c.drawRoundRect(7,7,17,17,2,2,ink);}else if(power){c.drawArc(4,4,20,20,-45,270,false,ink);c.drawLine(12,2,12,12,ink);}else{for(int y=6;y<=18;y+=6){c.drawLine(3,y,21,y,ink);c.drawCircle(y==12?15:8,y,2.5f,ink);}}c.restoreToCount(save);}
        public void setAlpha(int a){ink.setAlpha(a);}public void setColorFilter(android.graphics.ColorFilter f){ink.setColorFilter(f);}public int getOpacity(){return android.graphics.PixelFormat.TRANSLUCENT;}
    }
    @Override protected void onMeasure(int w,int h){
        boolean bounded=MeasureSpec.getMode(h)!=MeasureSpec.UNSPECIFIED;int height=MeasureSpec.getSize(h);boolean small=bounded&&height<p(360);
        if(small!=compact){compact=small;status.getLayoutParams().height=p(small?20:38);status.setMaxLines(small?1:2);status.setEllipsize(android.text.TextUtils.TruncateAt.END);}
        int fixed=p(48)+p(small?20:38)+p(104)+getPaddingTop()+getPaddingBottom();
        float size=bounded?Math.max(p(18),Math.min(p(54),(height-fixed)*.7f)):p(54);frequency.setTextSize(TypedValue.COMPLEX_UNIT_PX,size);super.onMeasure(w,h);
    }
}
