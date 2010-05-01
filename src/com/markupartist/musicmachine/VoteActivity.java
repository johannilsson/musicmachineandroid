package com.markupartist.musicmachine;

import java.io.IOException;

import com.markupartist.musicmachine.gateway.MusicMachineGateway;
import com.markupartist.musicmachine.gateway.MusicMachineGateway.UserHasAlreadyVotedException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class VoteActivity extends Activity implements OnClickListener {
    private Bundle mExtras;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vote);

        
        mExtras = getIntent().getExtras();
     
        TextView artistView = (TextView) findViewById(R.id.vote_artist);
        artistView.setText(mExtras.getString("mm.artist"));
        
        TextView titleView = (TextView) findViewById(R.id.vote_title);
        titleView.setText(mExtras.getString("mm.title"));
        
        TextView lengthView = (TextView) findViewById(R.id.vote_title);
        lengthView.setText("1:43");
        
        Button searchButton = (Button) findViewById(R.id.do_vote_button);
        searchButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.do_vote_button:

        	SharedPreferences sharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(this);

        	String userId = sharedPreferences.getString("username", "");

        	MusicMachineGateway mmGateway = new MusicMachineGateway(sharedPreferences.getString("server_url", ""));

        	try {
					mmGateway.vote(mExtras.getString("mm.uri"), userId);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UserHasAlreadyVotedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	
            Toast.makeText(this, "VOTE user id: " + userId, Toast.LENGTH_SHORT).show();
            break;
        }
        
    }
}