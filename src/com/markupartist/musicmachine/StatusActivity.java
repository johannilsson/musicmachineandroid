package com.markupartist.musicmachine;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class StatusActivity extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(this);
        
        Button historyButton = (Button) findViewById(R.id.history_button);
        historyButton.setOnClickListener(this);
        
        Button preferencesButton = (Button) findViewById(R.id.preferences_button);
        preferencesButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.search_button:
            Intent i = new Intent(this, SearchActivity.class);
            startActivity(i);
            break;
        case R.id.history_button:
        	break;
        case R.id.preferences_button:
        	break;
        }
    }
}