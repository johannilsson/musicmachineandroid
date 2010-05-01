package com.markupartist.musicmachine;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SearchActivity extends Activity implements OnClickListener {
    private static final String TAG = "Search";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        Button searchButton = (Button) findViewById(R.id.vote_button);
        searchButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.vote_button:
            Intent i = new Intent(this, VoteActivity.class);
            startActivity(i);
            break;
        }
        
    }
}