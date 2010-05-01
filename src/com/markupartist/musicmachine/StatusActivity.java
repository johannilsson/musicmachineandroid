package com.markupartist.musicmachine;

import android.app.Activity;
import android.app.ListActivity;
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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SimpleAdapter.ViewBinder;

import com.markupartist.musicmachine.gateway.MusicMachineGateway;
import com.markupartist.musicmachine.gateway.MusicMachineGateway.PlaylistTrack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StatusActivity extends ListActivity implements OnClickListener {
	private final static String TAG = StatusActivity.class.getSimpleName();
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

		currentSongName = (TextView) findViewById(R.id.currentSongName);
		currentSongArtist = (TextView) findViewById(R.id.currentSongArtist);
		currentSongAlbum = (TextView) findViewById(R.id.currentSongAlbum);
		currentSongTime = (TextView) findViewById(R.id.currentSongTime);

		// musicMachineGateway = new MusicMachineGateway();
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		String endpoint = sharedPreferences.getString("server_url",
				"http://10.0.2.1:8080");
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
		if (null != countDownTimer) {
			countDownTimer.cancel();
		}

		playlistRequestTimer.cancel();
	}

	private void requestPlaylist() {
		cancelTimers();

		try {
			List<PlaylistTrack> playlist = musicMachineGateway.getPlaylist();
			MusicMachineGateway.Status status = musicMachineGateway.getStatus();
			Log.d(TAG, playlist.toString());
			updatePlaylistInfo(playlist, status);
		} catch (IOException e) {
			// e.printStackTrace();
			Toast.makeText(this, "Error connecting to server",
					Toast.LENGTH_SHORT).show();

			// Try again in 10 seconds
			playlistRequestTimer.start();

			clearCurrentSongInfo();
		}
	}

	private void updatePlaylistInfo(
			final List<PlaylistTrack> playlist,
			final MusicMachineGateway.Status status) {

		Log.d(TAG, "Updating UI with " + playlist.size() + " songs.");

		if (playlist.size() > 0) {
			Iterator<PlaylistTrack> it = playlist.iterator();

			// Update current song
			PlaylistTrack song = it.next();
			currentSongName.setText(song.title);
			currentSongArtist.setText(song.artist);
			currentSongAlbum.setText(song.album);
			currentSongTime.setText("");

			countDownTimer = new SongCountDownTimer(status.timeUntilVote, 1000);
			countDownTimer.start();

			setListAdapter(createPlaylistAdapter(playlist.subList(1, playlist.size())));
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
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Map<String, Object> item = (Map<String, Object>) ((SimpleAdapter) getListAdapter())
				.getItem(position);
		MusicMachineGateway.PlaylistTrack track = (MusicMachineGateway.PlaylistTrack) item
				.get("track");
		Log.d(TAG, "track: " + track.toString());

		Intent i = new Intent(this, VoteActivity.class);
		i.putExtra("mm.artist", track.artist);
		i.putExtra("mm.title", track.title);
		i.putExtra("mm.uri", track.uri);
		// TODO: mm.length

		startActivity(i);

		super.onListItemClick(l, v, position, id);
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
		public void onTick(long millisUntilFinished) {
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
				R.layout.track_row, new String[] { "artist", "title" },
				new int[] { R.id.artist, R.id.title });

		adapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Object data,
					String textRepresentation) {
				PlaylistTrack song = (PlaylistTrack) data;
				
				switch (view.getId()) {
				case R.id.artist:
				case R.id.title:
					((TextView) view).setText(song.artist + " - " + song.title);
					return true;
				}
				return false;
			}
		});

		return adapter;
	}
}
