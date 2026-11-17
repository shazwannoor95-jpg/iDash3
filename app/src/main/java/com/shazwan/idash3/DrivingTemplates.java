package com.shazwan.idash3;

/** iDash-specific limits, independent of Apple's CarPlay APIs and entitlements. */
final class DrivingTemplates {
 static final int APPS=0,NOW_PLAYING=1,ACTIONS=2,TAB_COUNT=3;
 static final int TOUCH_DP=56,LIST_ITEMS=4,ALERT_ACTIONS=2;
 private DrivingTemplates(){}
 static int tab(int value){return value>=0&&value<TAB_COUNT?value:APPS;}
 static String choose(String saved,java.util.List<String> components,String[] preferred){return DrivePlan.keepOrPick(saved,components,preferred);}
}
