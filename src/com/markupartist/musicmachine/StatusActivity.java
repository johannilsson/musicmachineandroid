package com.markupartist.musicmachine;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SimpleAdapter.ViewBinder;

import com.markupartist.musicmachine.gateway.MusicMachineGateway;
import com.markupartist.musicmachine.gateway.SpotifyGatewayTrack;
import com.markupartist.musicmachine.gateway.MusicMachineGateway.PlaylistTrack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

public class StatusActivity extends ListActivity implements OnClickListener {
	private final static Integer RECONNECT_DELAY = 5; // Seconds
	private final static String TAG = StatusActivity.class.getSimpleName();
	private MusicMachineGateway musicMachineGateway;

	private SongCountDownTimer countDownTimer = null;
	private GetPlaylistTask getPlaylistTask;
	private GetStatusTask getStatusTask;
	private Handler handler = new Handler();
	private ConnectTask connectTask = new ConnectTask();

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

		currentSongName = (TextView) findViewById(R.id.currentSongTitle);
		currentSongArtist = (TextView) findViewById(R.id.currentSongArtist);
		currentSongAlbum = (TextView) findViewById(R.id.currentSongAlbum);
		currentSongTime = (TextView) findViewById(R.id.currentSongTime);

		// musicMachineGateway = new MusicMachineGateway();
		setupGateway();
		
		// Connect to the server
		handler.post(connectTask);
	}

	private void setupGateway() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		String endpoint = sharedPreferences.getString("server_url",
				"http://10.0.2.1:8080");
		musicMachineGateway = new MusicMachineGateway(endpoint);
	}

	@Override
	public void onDestroy() {
		handler.removeCallbacks(connectTask);
		
		if(null != getPlaylistTask) {
			getPlaylistTask.cancel(true);
		}
		
		if(null != getStatusTask) {
			getStatusTask.cancel(true);
		}
		
		cancelTimers();
		
		super.onDestroy();
	}
	
	private void cancelTimers() {
		if (null != countDownTimer) {
			countDownTimer.cancel();
		}
	}

	private void findServer() {
        try {
            JmDNS jmdns = JmDNS.create();
                ServiceInfo[] infos = jmdns.list("_http._tcp.local.");
                for (int i=0; i < infos.length; i++) {
                	// the service name is optional really, we shouldn't do this
                	if (infos[i].getName().equalsIgnoreCase("musicmachine")) {
                		// write new config. We're not supposed to do this either since
                		// we're supposed to be dynamic when using mdns
                		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                		sharedPreferences.edit().putString("server_url", String.format("http://%s:%d", infos[i].getHostAddress(), infos[i].getPort())).commit();
                		setupGateway();
                		Toast.makeText(this, "Found server!", Toast.LENGTH_SHORT).show();
                	}
                }
                System.out.println();
        } catch (IOException e) {
        	Toast.makeText(this, "Error while searching: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    	Toast.makeText(this, "No server found", Toast.LENGTH_SHORT).show();
		
	}


	private void requestPlaylist() {
		if(null == getPlaylistTask || getPlaylistTask.getStatus() == AsyncTask.Status.FINISHED) {
			getPlaylistTask = new GetPlaylistTask();
		}
		
		if(getPlaylistTask.getStatus() != AsyncTask.Status.RUNNING) {
			getPlaylistTask.execute();
		}
	}
	
	private void requestStatus() {
		if(null == getStatusTask || getStatusTask.getStatus() == AsyncTask.Status.FINISHED) {
			getStatusTask = new GetStatusTask();
		}
		
		if(getStatusTask.getStatus() != AsyncTask.Status.RUNNING) {
			getStatusTask.execute();
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
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Map<String, Object> item = (Map<String, Object>) ((SimpleAdapter) getListAdapter())
				.getItem(position);
		MusicMachineGateway.PlaylistTrack track = (MusicMachineGateway.PlaylistTrack) item
				.get("track");
		Log.d(TAG, "track: " + track.toString());

        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(track.uri));
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            showDialog(0);
        }

		super.onListItemClick(l, v, position, id);
	}

    @Override
    public Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch(id) {
            case 0: {
                builder.setTitle("Spotify not found");
                builder.setMessage("Spotify is needed to preview a song, please download it from the Android Market");
                builder.setPositiveButton("OK", null);
            }
        }

        return builder.create();
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
		case R.id.discover:
			findServer();
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

	private SimpleAdapter createPlaylistAdapter(List<PlaylistTrack> tracks) {
		ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		for (PlaylistTrack track : tracks) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("artist", track.artist);
			map.put("title", track.title);
			map.put("track", track);

			list.add(map);
		}

		SimpleAdapter adapter = new SimpleAdapter(this, list,
				R.layout.simple_track_row, new String[] { "artist", "title", },
				new int[] { R.id.artist, R.id.title });

		adapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Object data,
					String textRepresentation) {
				switch (view.getId()) {
                case R.id.artist:
                case R.id.title:
                    ((TextView)view).setText(textRepresentation);
                    return true;
                }
				return false;
			}
		});

		return adapter;
	}

	private class GetPlaylistTask extends AsyncTask<Void, Void, List<PlaylistTrack>> {

		@Override
		protected List<PlaylistTrack> doInBackground(Void... args) {
			try {
				List<PlaylistTrack> playlist = musicMachineGateway.getPlaylist();
				return playlist;
			} catch (IOException e) {
				Log.d(TAG, "Error fetching playlist");
				return null;
			}
		}

        @Override
        protected void onPostExecute(List<PlaylistTrack> result) {
            //dismissProgress();

            if (result != null) {
                onPlaylistReceived(result);
            } else {
            	onPlaylistError();
            }
        }
	}
	
	private void onPlaylistError() {
		clearCurrentSongInfo();
		connectTask.schedule(handler, RECONNECT_DELAY);
	}
	
	private void onPlaylistReceived(List<PlaylistTrack> playlist) {
		Log.d(TAG, "Updating UI with " + playlist.size() + " songs.");

		if (playlist.size() > 0) {
			PlaylistTrack song = playlist.get(0);
			currentSongName.setText(song.title);
			currentSongArtist.setText(song.artist);
			currentSongAlbum.setText(song.album);
			currentSongTime.setText("");

			setListAdapter(createPlaylistAdapter(playlist.subList(1, playlist.size())));
		} else {
			clearCurrentSongInfo();
			setListAdapter(createPlaylistAdapter(playlist));
		}
	}
	
	private class GetStatusTask extends AsyncTask<Void, Void, MusicMachineGateway.Status> {

		@Override
		protected MusicMachineGateway.Status doInBackground(Void... args) {
			try {
				MusicMachineGateway.Status status = musicMachineGateway.getStatus();
				return status;
			} catch (IOException e) {
				Log.d(TAG, "Error fetching status");
				return null;
			}
		}

        @Override
        protected void onPostExecute(MusicMachineGateway.Status result) {
            //dismissProgress();

            if (result != null) {
                onStatusReceived(result);
            } else {
            	onStatusError();
            }
        }
	}
	
	private void onStatusReceived(MusicMachineGateway.Status status) {
		countDownTimer = new SongCountDownTimer(status.timeUntilVote, 1000);
		countDownTimer.start();
	}
	
	private void onStatusError() {
		connectTask.schedule(handler, RECONNECT_DELAY);
	}
	
	class ConnectTask extends TimerTask {
		private boolean isScheduled = false;
		
		public void schedule(Handler handler, int seconds) {
			if(false == isScheduled) {
				Log.d(TAG, "Reconnecting in " + seconds + " seconds.");
				isScheduled = true;
				handler.removeCallbacks(this);
				handler.postDelayed(this, seconds * 1000);
			}
		}
		
		public void run() {
			isScheduled = false;
			
			// These calls will launch asynchronous requests to the server
			// so there's no need to worry that ConnectTask is executed on
			// the UI thread.
			requestPlaylist();
			requestStatus();
		}
	}
}
