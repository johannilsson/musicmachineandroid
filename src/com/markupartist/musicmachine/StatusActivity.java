package com.markupartist.musicmachine;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class StatusActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView textview = new TextView(this);
        String uID = android.provider.Settings.Secure.ANDROID_ID;
        textview.setText("Testar id: " + uID);
        setContentView(textview);
    }
}