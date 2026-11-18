package com.shazwan.idash3;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Eight apps per landscape page. Keeps only the visible page's views alive. */
final class AppLibraryGrid extends LinearLayout {
 static final int COLUMNS=4,ROWS=2,PER_PAGE=COLUMNS*ROWS;
 private final LauncherActivity a;
 private final ArrayList<LauncherActivity.App> apps=new ArrayList<>();
 private final LinearLayout cells,dots;
 private final FrameLayout viewport;
 private int page;private String focusedComponent;private float downX,downY;private boolean swiping;
 static int pages(int count){return Math.max(1,(count+PER_PAGE-1)/PER_PAGE);}
 AppLibraryGrid(LauncherActivity activity){
  super(activity);a=activity;setOrientation(VERTICAL);setContentDescription("Paged App Library");
  viewport=new FrameLayout(a){
   public boolean onInterceptTouchEvent(MotionEvent e){
    if(e.getActionMasked()==MotionEvent.ACTION_DOWN){downX=e.getX();downY=e.getY();swiping=false;}
    else if(e.getActionMasked()==MotionEvent.ACTION_MOVE&&Math.abs(e.getX()-downX)>ViewConfiguration.get(a).getScaledTouchSlop()&&Math.abs(e.getX()-downX)>Math.abs(e.getY()-downY)*1.3f){swiping=true;return true;}
    return false;
   }
   public boolean onTouchEvent(MotionEvent e){if(!swiping)return true;if(e.getActionMasked()==MotionEvent.ACTION_UP){if(Math.abs(e.getX()-downX)>a.dp(32))show(page+(e.getX()<downX?1:-1));swiping=false;}else if(e.getActionMasked()==MotionEvent.ACTION_CANCEL)swiping=false;return true;}
  };
  cells=new LinearLayout(a);cells.setOrientation(VERTICAL);viewport.addView(cells,new FrameLayout.LayoutParams(-1,-1));addView(viewport,new LayoutParams(-1,0,1));
  dots=new LinearLayout(a);dots.setGravity(Gravity.CENTER);addView(dots,new LayoutParams(-1,a.dp(48)));
  viewport.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{if(r-l!=or-ol||b-t!=ob-ot)sizeIcons();});
 }
 void setApps(List<LauncherActivity.App> list){boolean restore=hasFocus();apps.clear();apps.addAll(list);show(a.libraryPage);if(restore&&focusedComponent!=null){for(int i=page*PER_PAGE;i<Math.min(apps.size(),(page+1)*PER_PAGE);i++)if(focusedComponent.equals(apps.get(i).component)){focusIndex(i-page*PER_PAGE);break;}}}
 private void show(int requested){
  page=Math.max(0,Math.min(pages(apps.size())-1,requested));if(a.loaded||!apps.isEmpty())a.libraryPage=page;cells.removeAllViews();dots.removeAllViews();
  if(apps.isEmpty()){TextView empty=a.text(a.loaded?"No apps found. Try another search.":"Loading installed apps…",17,Color.WHITE);empty.setContentDescription("No app results");empty.setGravity(Gravity.CENTER);cells.addView(empty,new LayoutParams(-1,-1));return;}
  for(int row=0;row<ROWS;row++){
   LinearLayout line=new LinearLayout(a);line.setGravity(Gravity.CENTER);cells.addView(line,new LayoutParams(-1,0,1));
   for(int col=0;col<COLUMNS;col++){
    int index=page*PER_PAGE+row*COLUMNS+col;
    if(index>=apps.size()){line.addView(new View(a),new LayoutParams(0,-1,1));continue;}
    LauncherActivity.App app=apps.get(index);LinearLayout tile=new LinearLayout(a);tile.setOrientation(VERTICAL);tile.setGravity(Gravity.CENTER);tile.setContentDescription("Open "+app.label);focusable(tile);tile.setOnFocusChangeListener((v,has)->{if(has)focusedComponent=app.component;});
    ImageView icon=new ImageView(a);icon.setImageBitmap(app.icon);icon.setScaleType(ImageView.ScaleType.FIT_CENTER);tile.addView(icon,new LayoutParams(a.dp(88),a.dp(88)));
    TextView label=a.text(app.label,17,Color.WHITE);label.setMaxLines(getResources().getConfiguration().fontScale>1.2f?2:1);label.setIncludeFontPadding(false);label.setEllipsize(TextUtils.TruncateAt.END);label.setGravity(Gravity.CENTER);label.setShadowLayer(a.dp(2),0,a.dp(1),0x88000000);
    LayoutParams lp=new LayoutParams(-1,labelHeight());lp.topMargin=a.dp(6);tile.addView(label,lp);
    tile.setOnClickListener(v->a.launch(app));tile.setOnLongClickListener(v->{new android.app.AlertDialog.Builder(a).setTitle("Add "+app.label+" to favorites").setItems(a.slots,(d,which)->a.assign(which,app)).show();return true;});
    final int column=col;final int rowIndex=row;
    tile.setOnKeyListener((v,key,event)->{
     int direction=key==KeyEvent.KEYCODE_DPAD_RIGHT&&column==COLUMNS-1?1:key==KeyEvent.KEYCODE_DPAD_LEFT&&column==0?-1:0;
     if(direction==0||page+direction<0||page+direction>=pages(apps.size()))return false;
     if(event.getAction()==KeyEvent.ACTION_DOWN){show(page+direction);int target=Math.min(apps.size()-page*PER_PAGE-1,rowIndex*COLUMNS+(direction>0?0:COLUMNS-1));focusIndex(target);}return true;
    });
    line.addView(tile,new LayoutParams(0,-1,1));
   }
  }
  TextView previous=a.text("‹",28,Color.WHITE);previous.setGravity(Gravity.CENTER);focusable(previous);previous.setContentDescription("Previous app page");previous.setEnabled(page>0);previous.setAlpha(page>0?1:.3f);previous.setOnClickListener(v->show(page-1));dots.addView(previous,new LayoutParams(a.dp(48),-1));
  // A bounded indicator remains usable even with hundreds of installed apps.
  int total=pages(apps.size()),first=Math.max(0,Math.min(page-2,total-5));
  for(int i=first;i<Math.min(total,first+5);i++){final int target=i;FrameLayout hit=new FrameLayout(a);focusable(hit);hit.setContentDescription("App page "+(i+1)+" of "+total);View dot=new View(a);GradientDrawable bg=new GradientDrawable();bg.setShape(GradientDrawable.OVAL);bg.setColor(i==page?Color.WHITE:0x66ffffff);dot.setBackground(bg);hit.addView(dot,new FrameLayout.LayoutParams(a.dp(10),a.dp(10),Gravity.CENTER));hit.setOnClickListener(v->show(target));dots.addView(hit,new LayoutParams(a.dp(48),-1));}
  TextView next=a.text("›",28,Color.WHITE);next.setGravity(Gravity.CENTER);focusable(next);next.setContentDescription("Next app page");next.setEnabled(page<total-1);next.setAlpha(page<total-1?1:.3f);next.setOnClickListener(v->show(page+1));dots.addView(next,new LayoutParams(a.dp(48),-1));sizeIcons();
 }
 private void focusable(View v){
  v.setFocusable(true);android.graphics.drawable.StateListDrawable states=new android.graphics.drawable.StateListDrawable();
  GradientDrawable selected=new GradientDrawable();selected.setColor(0x553b83d5);selected.setCornerRadius(a.dp(12));selected.setStroke(a.dp(3),Color.WHITE);
  states.addState(new int[]{android.R.attr.state_focused},selected);states.addState(new int[]{android.R.attr.state_pressed},selected);states.addState(new int[]{},new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));v.setBackground(states);
 }
 private int labelHeight(){return Math.max(a.dp(28),(int)Math.ceil(17*getResources().getDisplayMetrics().scaledDensity*1.35f)*(getResources().getConfiguration().fontScale>1.2f?2:1));}
 private void focusIndex(int index){int r=index/COLUMNS,c=index%COLUMNS;if(index>=0&&r<cells.getChildCount()&&cells.getChildAt(r) instanceof LinearLayout){ViewGroup row=(ViewGroup)cells.getChildAt(r);if(c<row.getChildCount()){View target=row.getChildAt(c);if(target.isLaidOut())target.requestFocus();else target.addOnLayoutChangeListener(new View.OnLayoutChangeListener(){public void onLayoutChange(View v,int l,int t,int right,int b,int ol,int ot,int or,int ob){if(right>l&&b>t){v.removeOnLayoutChangeListener(this);v.requestFocus();}}});}}}
 private void sizeIcons(){
  int size=Math.max(a.dp(24),Math.min(a.dp(120),Math.min(viewport.getWidth()/COLUMNS-a.dp(24),viewport.getHeight()/ROWS-labelHeight()-a.dp(10))));
  for(int r=0;r<cells.getChildCount();r++)if(cells.getChildAt(r) instanceof LinearLayout){LinearLayout row=(LinearLayout)cells.getChildAt(r);for(int c=0;c<row.getChildCount();c++)if(row.getChildAt(c) instanceof LinearLayout){LinearLayout tile=(LinearLayout)row.getChildAt(c);View icon=tile.getChildAt(0);if(icon.getLayoutParams().width!=size){icon.getLayoutParams().width=size;icon.getLayoutParams().height=size;icon.requestLayout();}}}
 }
}
