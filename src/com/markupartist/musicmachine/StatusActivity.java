package com.markupartist.musicmachine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

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
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SimpleAdapter.ViewBinder;

import com.markupartist.musicmachine.gateway.LastFMGateway;
import com.markupartist.musicmachine.gateway.LastFMGatewayAlbum;
import com.markupartist.musicmachine.gateway.MusicMachineGateway;
import com.markupartist.musicmachine.gateway.LastFMGateway.LastFMGatewayException;
import com.markupartist.musicmachine.gateway.LastFMGatewayAlbumParser.LastFMGatewayParseException;
import com.markupartist.musicmachine.gateway.MusicMachineGateway.PlaylistTrack;
import com.markupartist.musicmachine.utils.ImageLoader;

public class StatusActivity extends ListActivity implements OnClickListener {
	private final static Integer RECONNECT_DELAY = 5; // Seconds
	private final static String TAG = StatusActivity.class.getSimpleName();
	private MusicMachineGateway musicMachineGateway;
	private LastFMGateway lastFMGateway = new LastFMGateway();

	private SongCountDownTimer countDownTimer = null;
	private GetPlaylistTask getPlaylistTask;
	private GetStatusTask getStatusTask;
	private GetAlbumArtTask getAlbumArtTask;
	private Handler handler = new Handler();
	private ConnectTask connectTask = new ConnectTask();

	// Widgets
	private TextView currentSongName;
	private TextView currentSongArtist;
	private TextView currentSongAlbum;
	private TextView currentSongTime;
	private ImageView albumArtView;

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
		albumArtView = (ImageView) findViewById(R.id.albumArt);

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
		
		if(null != connectTask) {
			connectTask.cancel();
		}
		
		if(null != getPlaylistTask) {
			getPlaylistTask.cancel(true);
		}
		
		if(null != getStatusTask) {
			getStatusTask.cancel(true);
		}
		
		if(null != getAlbumArtTask) {
			getAlbumArtTask.cancel(true);
		}
	
		if(true == ImageLoader.hasInstance()) {
			ImageLoader.getInstance().cancel();
		}
		
		if (null != countDownTimer) {
			countDownTimer.cancel();
		}
		
		super.onDestroy();
	}

	private void findServer() {
	    boolean foundServer = false;
        try {
            JmDNS jmdns = JmDNS.create();
            ServiceInfo[] infos = jmdns.list("_http._tcp.local.");
            for (int i = 0; i < infos.length; i++) {
                // HACK: Anything on this port should be a music machine
                if (infos[i].getPort() == 6170) {
                    // write new config. We're not supposed to do this either since
                    // we're supposed to be dynamic when using mdns
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                    sharedPreferences.edit().putString("server_url", String.format("http://%s:%d", infos[i].getHostAddress(), infos[i].getPort())).commit();
                    setupGateway();
                    foundServer = true;
                }
            }
        } catch (IOException e) {
        	Toast.makeText(this, "Error while searching: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        if (foundServer) {
            Toast.makeText(this, "Found server!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No server found", Toast.LENGTH_SHORT).show();
        }
	}

	/**
	 * Perform an asynchronous playlist request if one is not already
	 * executing.
	 */
	public void requestPlaylist() {
		if(null == getPlaylistTask || getPlaylistTask.getStatus() == AsyncTask.Status.FINISHED) {
			getPlaylistTask = new GetPlaylistTask();
		}
		
		if(getPlaylistTask.getStatus() != AsyncTask.Status.RUNNING) {
			getPlaylistTask.execute();
		}
	}

	/**
	 * Perform an asynchronous status request if one is not already executing.
	 */
	public void requestStatus() {
		if(null == getStatusTask || getStatusTask.getStatus() == AsyncTask.Status.FINISHED) {
			getStatusTask = new GetStatusTask();
		}
		
		if(getStatusTask.getStatus() != AsyncTask.Status.RUNNING) {
			getStatusTask.execute();
		}
	}

	/**
	 * Perform an asynchronous album art fetch request if one is not already executing.
	 */
	private void requestAlbumArt(MusicMachineGateway.PlaylistTrack song) {
		if(null == getAlbumArtTask || getAlbumArtTask.getStatus() == AsyncTask.Status.FINISHED) {
			getAlbumArtTask = new GetAlbumArtTask();
		}
		
		if(getAlbumArtTask.getStatus() != AsyncTask.Status.RUNNING) {
			getAlbumArtTask.execute(song);
		}
	}

	private void clearCurrentSongInfo() {
		currentSongName.setText("");
		currentSongArtist.setText("");
		currentSongAlbum.setText("");
		currentSongTime.setText("");
		albumArtView.setImageResource(R.drawable.android_cover_small);
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
			requestStatus();
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
			map.put("artist_title", String.format("%s • %s", track.artist, track.title));
			map.put("track", track);

			list.add(map);
		}

		SimpleAdapter adapter = new SimpleAdapter(this, list,
				R.layout.simple_track_row, new String[] {"artist_title",},
				new int[] { R.id.artist_title });

		adapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Object data,
					String textRepresentation) {
				switch (view.getId()) {
                case R.id.artist_title:
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
			requestAlbumArt(song);
			currentSongName.setText(song.title);
			currentSongArtist.setText(song.artist);
			currentSongAlbum.setText(song.album);

			setListAdapter(createPlaylistAdapter(playlist.subList(1, playlist.size())));
		} else {
			clearCurrentSongInfo();
			setListAdapter(createPlaylistAdapter(playlist));
		}
	}

	/**
	 * Task responsible for retrieving status info about the current playing
	 * song. After receiving a status update it will initiate the count down
	 * timer.
	 */
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
        protected void onPostExecute(final MusicMachineGateway.Status result) {
            //dismissProgress();

            if (result != null) {
                onStatusReceived(result);
            } else {
            	onStatusError();
            }
        }
	}
	
	/**
	 * Task responsible for downloading album art from LastFM. If multiple
	 * covers are returned from LastFM then the first found cover will be
	 * taken. The default cover will be set on parse error or if no cover
	 * data (album/artist) is specified.
	 */
	private class GetAlbumArtTask extends AsyncTask<MusicMachineGateway.PlaylistTrack, Void, String> {

		@Override
		protected String doInBackground(MusicMachineGateway.PlaylistTrack... args) {
			if(null == args || args.length == 0) {
				return null;
			}
			
			MusicMachineGateway.PlaylistTrack track = (MusicMachineGateway.PlaylistTrack) args[0];

			try {
				List<LastFMGatewayAlbum> albums = lastFMGateway.searchAlbum(track.artist, track.album);
				
				// If we got multiple replies back then just snag the first
				// one. 
				if (albums != null && albums.size() > 0) {
					return albums.get(0).coverURL;
				}
			} catch (LastFMGatewayException e) {
				Log.e(TAG, e.toString());
			} catch (LastFMGatewayParseException e) {
				Log.e(TAG, e.toString());
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(final String coverURL) {
			if (null != coverURL && false == TextUtils.isEmpty(coverURL)) {
			    ImageLoader.getInstance().load(albumArtView, coverURL, true, R.drawable.android_cover_small, null);
			} else {
				albumArtView.setImageResource(R.drawable.android_cover_small);
			}
		}
	}

	/**
	 * Status update. Starts the count down timer to fetch the next status
	 * update. See {@link SongCountDownTimer}.
	 * @param status Status update received from server.
	 */
	private void onStatusReceived(final MusicMachineGateway.Status status) {
		countDownTimer = new SongCountDownTimer(status.timeUntilVote, 1000);
		countDownTimer.start();
	}

	/**
	 * Status error handler. This will trigger a reconnect attempt.
	 */
	private void onStatusError() {
		connectTask.schedule(handler, RECONNECT_DELAY);
	}
	
	class ConnectTask extends TimerTask {
		private boolean isScheduled = false;
		
		/**
		 * Schedule the task to execute in n seconds. If the task is already
		 * scheduled then this will do nothing.
		 * @param handler Handler to attach to.
		 * @param seconds Execution delay.
		 */
		public void schedule(Handler handler, int seconds) {
			if(false == isScheduled) {
				Log.d(TAG, "Reconnecting in " + seconds + " seconds.");
				isScheduled = true;
				handler.removeCallbacks(this);
				handler.postDelayed(this, seconds * 1000L);
			} else {
				Log.d(TAG, "Connect task already scheduled. Ignoring.");
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
