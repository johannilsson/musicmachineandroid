package com.markupartist.musicmachine;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.markupartist.musicmachine.gateway.MusicMachineGateway;
import com.markupartist.musicmachine.gateway.SpotifyGateway;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class StatusActivity extends Activity implements OnClickListener {
	private final static String LOG_TAG = StatusActivity.class.getSimpleName();
	private MusicMachineGateway musicMachineGateway;
	
	Timer countdownTimer;
	Timer statusRequestTimer;
	
	// Widgets
	private TextView currentSongName;
	private TextView currentSongArtist;
	private TextView currentSongAlbum;
	private TextView currentSongTime;
	
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
        
        currentSongName = (TextView) findViewById(R.id.currentSongName);
        currentSongArtist = (TextView) findViewById(R.id.currentSongArtist);
        currentSongAlbum = (TextView) findViewById(R.id.currentSongAlbum);
        currentSongTime = (TextView) findViewById(R.id.currentSongTime);
        
        musicMachineGateway = new MusicMachineGateway();
        //requestPlaylist();
        
        statusRequestTimer = new Timer();
        statusRequestTimer.scheduleAtFixedRate(new CountdownTask(), 0, 1000);
    }
    
    private void requestPlaylist() {        
        try {
            List<MusicMachineGateway.PlaylistTrack> playlist = musicMachineGateway.getPlaylist();
        	MusicMachineGateway.Status status = musicMachineGateway.getStatus();
            Log.d(LOG_TAG, playlist.toString());
        	updatePlaylistInfo(playlist, status);
        } catch (IOException e) {
			e.printStackTrace();
			clearCurrentSongInfo();
		}
    }
    
    private void updatePlaylistInfo(
    		final List<MusicMachineGateway.PlaylistTrack> playlist,
    		final MusicMachineGateway.Status status) {
    	
    	runOnUiThread(new Runnable(){

			@Override
			public void run() {
				try {
		        	MusicMachineGateway.PlaylistTrack song = playlist.get(0);
	
		        	currentSongName.setText(song.title);
		        	currentSongArtist.setText(song.artist);
		        	currentSongAlbum.setText(song.album);
		        	
		        	int minutes = status.timeUntilVote / 60000;
		        	int seconds = status.timeUntilVote % 60000 / 1000;
		        	currentSongTime.setText(String.format("%d:%02d", minutes, seconds));
				} catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
		        	clearCurrentSongInfo();
				}
			}
    	});
    }
    
    private void clearCurrentSongInfo() {
    	runOnUiThread(new Runnable() {

			@Override
			public void run() {
		    	currentSongName.setText("");
		    	currentSongArtist.setText("");
		    	currentSongAlbum.setText("");
		    	currentSongTime.setText("");
			}
    	});
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
    
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.myprefspane:
            	startActivity(new Intent(this, PrefsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private class CountdownTask extends TimerTask {

		@Override
		public void run() {
			requestPlaylist();
		}
    }
}
