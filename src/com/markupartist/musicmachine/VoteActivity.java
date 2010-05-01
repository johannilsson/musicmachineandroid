package com.markupartist.musicmachine;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.markupartist.musicmachine.gateway.MusicMachineGateway;
import com.markupartist.musicmachine.gateway.MusicMachineGateway.UserHasAlreadyVotedException;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class VoteActivity extends Activity implements OnClickListener {
    private Bundle mExtras;
    private String spotifyUri;
    private boolean mFromShare = false;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vote);
        mExtras = getIntent().getExtras();

        /*
         * Did this come from spotify?
         */
        
        if (getIntent().getAction() != null && getIntent().getAction().equals(Intent.ACTION_SEND)) {
        	CharSequence url = getIntent().getCharSequenceExtra(Intent.EXTRA_TEXT);
        	CharSequence subject = getIntent().getCharSequenceExtra(Intent.EXTRA_SUBJECT);
        	
        	mFromShare = true;
        	
        	// subject = "Cobrastyle - Alt. Version - Robyn"
        	
        	int li = subject.toString().lastIndexOf("-");

        	String title = subject.subSequence(0, li-1).toString();
        	String artist = subject.subSequence(li+2, subject.length()).toString();
        	
        	String id = url.subSequence(url.toString().lastIndexOf("/")+1, url.length()).toString();
        	String uri = "spotify:track:" + id;
        	
        	mExtras.putString("mm.artist", artist);
        	mExtras.putString("mm.title", title);
        	mExtras.putString("mm.album", "-");
        	mExtras.putString("mm.uri", uri);
        	//Toast.makeText(this, String.format("%s, %s (%s) (%s)", artist, title, url, uri), Toast.LENGTH_LONG).show();
        }
        
        spotifyUri = mExtras.getString("mm.uri");
     
        TextView artistView = (TextView) findViewById(R.id.vote_artist);
        artistView.setText(mExtras.getString("mm.artist"));
        
        TextView titleView = (TextView) findViewById(R.id.vote_title);
        titleView.setText(mExtras.getString("mm.title"));

        TextView albumView = (TextView) findViewById(R.id.vote_disc);
        albumView.setText(mExtras.getString("mm.album"));
        
        TextView lengthView = (TextView) findViewById(R.id.vote_length);
        double length = mExtras.getDouble("mm.length");
        int minutes = (int) (length / 60);
        int seconds = (int) (length % 60);
        lengthView.setText(String.format("%d:%02d", minutes, seconds));
        
        Button searchButton = (Button) findViewById(R.id.do_vote_button);
        searchButton.setOnClickListener(this);

        Button previewButton = (Button) findViewById(R.id.preview_button);
        previewButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.preview_button:
            try {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(spotifyUri));
                startActivity(i);
            } catch (ActivityNotFoundException e) {
                showDialog(0);
            }
            break;

        case R.id.do_vote_button:
            VoteTask voteTask = new VoteTask();
            voteTask.execute("");
            break;
        }
        
    }

    @Override
    public Dialog onCreateDialog(int id)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch(id)
        {
            case 0:
            {
                builder.setTitle("Spotify not found");
                builder.setMessage("Spotify is needed to preview a song, please download it from the Android Market");
                builder.setPositiveButton("OK", null);
            }
        }

        return builder.create();
    }

    /**
     * Background job for voting...
     */
    private class VoteTask extends AsyncTask<String, Void, Boolean> {
        private boolean mWasSuccess = true;
        private String userErrorMessage = "";

        @Override
        public void onPreExecute() {
            //showProgress();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(VoteActivity.this);

            String userId = "";
            TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            String deviceId = telephonyManager.getDeviceId();
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                digest.update(deviceId.getBytes());
                byte messageDigest[] = digest.digest();

                // Create Hex String
                StringBuffer hexString = new StringBuffer();
                for (int i=0; i<messageDigest.length; i++)
                    hexString.append(Integer.toHexString(0xFF & messageDigest[i]));

                userId = hexString.toString();
            } catch (NoSuchAlgorithmException e) {
                Log.e("VoteTask", "MessageDigest.getInstance", e);
            }

            Log.d("VoteTask", "UserId: " + userId);

            MusicMachineGateway mmGateway = new MusicMachineGateway(sharedPreferences.getString("server_url", ""));

            try {
                mmGateway.vote(mExtras.getString("mm.uri"), userId);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                userErrorMessage = "Opps, the music machine is gone!";
                return false;
            } catch (UserHasAlreadyVotedException e) {
                // TODO Auto-generated catch block
                userErrorMessage = "You have already voted, you have to wait until song.";
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result == true) {
                Toast.makeText(VoteActivity.this, "Thank you!", Toast.LENGTH_SHORT).show();
                
                if (mFromShare) {
                    finish();
                    return;
                } else {
                    Intent i = new Intent(VoteActivity.this, StatusActivity.class);
                    startActivity(i);   
                }
                
            } else {
                Toast.makeText(VoteActivity.this, userErrorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }
}