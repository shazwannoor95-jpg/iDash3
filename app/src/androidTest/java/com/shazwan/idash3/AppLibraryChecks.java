package com.shazwan.idash3;
import android.app.*;import android.content.*;import android.os.*;import android.view.*;import android.widget.*;import java.util.*;
/** Verifies eight-cell pagination, search, and physical bounds at both head-unit resolutions. */
public class AppLibraryChecks extends Instrumentation {
 LauncherActivity a;int count;
 void check(boolean yes,String text){if(!yes)throw new AssertionError(text);count++;}
 void ui(Runnable work){Throwable[] error={null};runOnMainSync(()->{try{work.run();}catch(Throwable t){error[0]=t;}});waitForIdleSync();if(error[0]!=null)throw new AssertionError(error[0]);}
 View find(View v,String text){if(text.equals(v.getContentDescription()==null?"":v.getContentDescription().toString()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View hit=find(g.getChildAt(i),text);if(hit!=null)return hit;}}return null;}
 public void onCreate(Bundle args){super.onCreate(args);start();}
 public void onStart(){Bundle out=new Bundle();int code=Activity.RESULT_CANCELED;try{
  a=(LauncherActivity)startActivitySync(new Intent(getTargetContext(),LauncherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
  java.util.concurrent.CountDownLatch ready=new java.util.concurrent.CountDownLatch(1);a.worker.execute(()->a.main.post(ready::countDown));check(ready.await(45,java.util.concurrent.TimeUnit.SECONDS),"discovery barrier");waitForIdleSync();
  ui(()->{a.page="apps";a.render();check(a.grid instanceof AppLibraryGrid,"Apps opens paged library");});
  ui(()->{
   List<LauncherActivity.App> list=new ArrayList<>();for(int i=0;i<17;i++)list.add(new LauncherActivity.App("Test "+i,"test/.App"+i,null));
   for(int[] size:new int[][]{{1024,600},{800,480}}){
    AppLibraryGrid grid=new AppLibraryGrid(a);grid.setApps(list);grid.measure(View.MeasureSpec.makeMeasureSpec(size[0],View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(size[1]-220,View.MeasureSpec.EXACTLY));grid.layout(0,0,size[0],size[1]-220);
    check(find(grid,"Open Test 0")!=null&&find(grid,"Open Test 7")!=null&&find(grid,"Open Test 8")==null,"exactly first eight apps visible");
    View first=find(grid,"Open Test 0"),fourth=find(grid,"Open Test 3"),fifth=find(grid,"Open Test 4");View rowOne=(View)first.getParent(),rowTwo=(View)fifth.getParent();
    check(first.getTop()==fourth.getTop()&&rowTwo.getTop()>rowOne.getTop()&&fourth.getLeft()>first.getLeft(),"four columns and second row");
    ImageView icon=(ImageView)((ViewGroup)first).getChildAt(0);check(icon.getLayoutParams().width>52&&icon.getLayoutParams().width<=120,"large bounded icons");
    find(grid,"Next app page").performClick();check(find(grid,"Open Test 8")!=null&&find(grid,"Open Test 15")!=null&&find(grid,"Open Test 0")==null,"second page uses next eight apps");
    find(grid,"Next app page").performClick();check(find(grid,"Open Test 16")!=null&&!find(grid,"Next app page").isEnabled(),"last partial page bounded");
    grid.setApps(Collections.singletonList(list.get(5)));check(find(grid,"Open Test 5")!=null&&find(grid,"Open Test 16")==null,"filter resets page");
    grid.setApps(Collections.emptyList());check(find(grid,"Next app page")==null,"empty results have no stale paging controls");
   }
  });
  ui(()->{a.search.setText("unlikely-nonexistent-app-987654");check(a.drawerCount.getText().toString().equals("0 apps"),"search filters actual app list");a.search.setText("");});
  out.putString("stream","PASS: "+count+" app library assertions\n");code=Activity.RESULT_OK;
 }catch(Throwable t){out.putString("stream","FAIL after "+count+": "+t+"\n");}finally{if(a!=null)ui(a::finish);}finish(code,out);}
}
