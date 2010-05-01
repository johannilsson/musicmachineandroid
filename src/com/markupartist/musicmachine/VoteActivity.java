package com.markupartist.musicmachine;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class VoteActivity extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vote);

        Button searchButton = (Button) findViewById(R.id.do_vote_button);
        searchButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.do_vote_button:
            Toast.makeText(this, "VOTE VOTE!", Toast.LENGTH_SHORT).show();
            break;
        }
        
    }
}