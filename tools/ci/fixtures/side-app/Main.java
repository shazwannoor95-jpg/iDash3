package com.dev.bitchord;
public class Main extends android.app.Activity {public void onCreate(android.os.Bundle b){super.onCreate(b);android.widget.TextView t=new android.widget.TextView(this);t.setText("SPLIT TEST FIXTURE\nNot the BitChord app\n\nThis verifies a separate app window.");t.setGravity(17);t.setTextSize(20);t.setTextColor(-1);t.setBackgroundColor(0xff172335);setContentView(t);}}
