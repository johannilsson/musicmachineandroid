package com.markupartist.musicmachine;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.markupartist.musicmachine.gateway.MusicMachineGateway;
import com.markupartist.musicmachine.gateway.SpotifyGateway;
import com.markupartist.musicmachine.gateway.MusicMachineGateway.PlaylistTrack;
import com.markupartist.musicmachine.gateway.SpotifyGatewayTrack;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class StatusActivity extends Activity implements OnClickListener {
	private final static String LOG_TAG = StatusActivity.class.getSimpleName();
	private MusicMachineGateway musicMachineGateway;

	private SongCountDownTimer countDownTimer = null;
	private PlaylistRequestTimer playlistRequestTimer;
	
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
        
        //musicMachineGateway = new MusicMachineGateway();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String endpoint = sharedPreferences.getString("server_url", "http://10.0.2.1:8080");
        musicMachineGateway = new MusicMachineGateway(endpoint);
        playlistRequestTimer = new PlaylistRequestTimer(10000, 1000);
        requestPlaylist();
    }
    
    @Override
    public void onDestroy() {
    	cancelTimers();
    	super.onDestroy();
    }
    
    @Override
    public void onPause() {
    	cancelTimers();
    	super.onPause();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	requestPlaylist();
    }
    
    private void cancelTimers() {
    	if(null != countDownTimer) {
    		countDownTimer.cancel();
    	}
    	
    	playlistRequestTimer.cancel();
    }
    
    private void requestPlaylist() {
    	cancelTimers();
    	
        try {        	
            List<MusicMachineGateway.PlaylistTrack> playlist = musicMachineGateway.getPlaylist();
        	MusicMachineGateway.Status status = musicMachineGateway.getStatus();
            Log.d(LOG_TAG, playlist.toString());
        	updatePlaylistInfo(playlist, status);
        } catch (IOException e) {
			//e.printStackTrace();
        	Toast.makeText(this, "Error connecting to server", Toast.LENGTH_SHORT).show();
        	
        	// Try again in 10 seconds
        	playlistRequestTimer.start();
        	
			clearCurrentSongInfo();
		}
    }
    
    private void updatePlaylistInfo(
    		final List<MusicMachineGateway.PlaylistTrack> playlist,
    		final MusicMachineGateway.Status status) {
    	
    	Log.d(LOG_TAG, "Updating UI with " + playlist.size() + " songs.");    	
    	
		if(playlist.size() > 0) {
			Iterator<PlaylistTrack> it = playlist.iterator();
			
			// Update current song
        	MusicMachineGateway.PlaylistTrack song = it.next();
        	currentSongName.setText(song.title);
        	currentSongArtist.setText(song.artist);
        	currentSongAlbum.setText(song.album);
        	currentSongTime.setText("");
        	
        	countDownTimer = new SongCountDownTimer(status.timeUntilVote, 1000); 
        	countDownTimer.start();
        	
        	// Update playlist
        	while(it.hasNext()) {
        		song = it.next();
        	}
		} else {
			clearCurrentSongInfo();
			
        	// Try again in 10 seconds
        	playlistRequestTimer.start();
		}
    }
    
    private void clearCurrentSongInfo() {
    	currentSongName.setText("");
    	currentSongArtist.setText("");
    	currentSongAlbum.setText("");
    	currentSongTime.setText("");
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
    
    private class SongCountDownTimer extends CountDownTimer {
    	public SongCountDownTimer(long millisInFuture, long countDownInterval) {
    		super(millisInFuture, countDownInterval);
        }
    	
		@Override
		public void onFinish() {
			requestPlaylist();
		}

		@Override
		public void onTick(long millisUntilFinished) {
			int minutes = (int) (millisUntilFinished / 60000);
        	int seconds = (int) (millisUntilFinished % 60000 / 1000);
        	currentSongTime.setText(String.format("%d:%02d", minutes, seconds));
		}
    }
    
    private class PlaylistRequestTimer extends CountDownTimer {
    	public PlaylistRequestTimer(long millisInFuture, long countDownInterval) {
    		super(millisInFuture, countDownInterval);
        }
    	
		@Override
		public void onFinish() {
			requestPlaylist();
		}

		@Override
		public void onTick(long millisUntilFinished) {}
    	
    }
}
