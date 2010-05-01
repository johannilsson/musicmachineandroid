package com.markupartist.musicmachine;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SearchActivity extends ListActivity implements OnClickListener {
    private static final String TAG = "Search";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
    }

    @Override
    public void onClick(View v) {
        /*
        switch (v.getId()) {
        case R.id.vote_button:
            Intent i = new Intent(this, VoteActivity.class);
            startActivity(i);
            break;
        }
        */
    }
}