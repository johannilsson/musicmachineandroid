package com.markupartist.musicmachine.gateway;

import java.util.ArrayList;
import java.util.List;

/*
 * Copyright: ANARCHY! YEAH!
 */
public class MusicMachineGateway {
	private String serverHost = "127.0.0.1";
	private int serverPort = 8080;
	
	public MusicMachineGateway() {
		
	}

	public MusicMachineGateway(String serverHost, int serverPort) {
		this.serverHost = serverHost;
		this.serverPort = serverPort;
	}
	
	public Status getStatus() {
		Status s = new Status();
		s.playedTimeMillis = 10000;
		s.timeUntilVoteMillis = 20000;
		return s;
	}
	
	public List<PlaylistTrack> getPlaylist() {
		ArrayList<PlaylistTrack> tracks = new ArrayList<PlaylistTrack>();
		
		PlaylistTrack t1 = new PlaylistTrack();
		t1.artist = "Foo Fighters";
		t1.title = "All My Life";
		t1.lengthMillis = 142 * 1000;
		t1.uri = "spotify:track:madeup";
		
		tracks.add(t1);
		return tracks;
	}

	public List<PlaylistTrack> getPreviousTracks() {
		ArrayList<PlaylistTrack> tracks = new ArrayList<PlaylistTrack>();
		
		PlaylistTrack t1 = new PlaylistTrack();
		t1.artist = "Foo Fighters";
		t1.title = "All My Life";
		t1.lengthMillis = 142 * 1000;
		t1.uri = "spotify:track:madeup";
		
		tracks.add(t1);
		return tracks;
	}
	
	public class Status {
		public int playedTimeMillis;
		public int timeUntilVoteMillis;
	}
	
	public class PlaylistTrack {
		public String artist;
		public String title;
		public int lengthMillis;
		public String uri;
	}
}
