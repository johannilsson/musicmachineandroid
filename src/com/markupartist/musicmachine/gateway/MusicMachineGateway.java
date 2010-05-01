package com.markupartist.musicmachine.gateway;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/*
 * Copyright: ANARCHY! YEAH!
 */
public class MusicMachineGateway {
	private String serverHost = "127.0.0.1";
	private int serverPort = 8080;
	Gson gson = new Gson();
	
	public MusicMachineGateway() {
		
	}

	public MusicMachineGateway(String serverHost, int serverPort) {
		this.serverHost = serverHost;
		this.serverPort = serverPort;
	}
	
	public Status getStatus() {
		String statusString = "{\"playtime\":0,\"timeUntilVote\":30000,\"numVotes\":0}";
		Status s = gson.fromJson(statusString, Status.class);
		return s;
	}
	
	public List<PlaylistTrack> getPlaylist() {
		String playlistString = "[{\"id\":\"9019ff7107c74cc68e098085e80b084d\",\"artist\":\"Foo Fighters\",\"title\":\"Best Of You\",\"uri\":\"spotify:track:4nUM7pGcTUK2pY1d2LybrT\",\"length\":256600},{\"id\":\"fd3f7007e8a04960b223938e3df496eb\",\"artist\":\"Foo Fighters\",\"title\":\"Wheels\",\"uri\":\"spotify:track:7HS35r2OVvrj40GxdQqd6P\",\"length\":277693}]";
		Type collectionType = new TypeToken<List<PlaylistTrack>>(){}.getType();
		List<PlaylistTrack> tracks = gson.fromJson(playlistString, collectionType);
		return tracks;
	}

	public List<PlaylistTrack> getPreviousTracks() {
		String playlistString = "[{\"id\":\"9019ff7107c74cc68e098085e80b084d\",\"artist\":\"Foo Fighters\",\"title\":\"Best Of You\",\"uri\":\"spotify:track:4nUM7pGcTUK2pY1d2LybrT\",\"length\":256600},{\"id\":\"fd3f7007e8a04960b223938e3df496eb\",\"artist\":\"Foo Fighters\",\"title\":\"Wheels\",\"uri\":\"spotify:track:7HS35r2OVvrj40GxdQqd6P\",\"length\":277693}]";
		Type collectionType = new TypeToken<List<PlaylistTrack>>(){}.getType();
		List<PlaylistTrack> tracks = gson.fromJson(playlistString, collectionType);
		return tracks;
	}
	
	static public class Status {
		Status() {}
		public int playtime;
		public int timeUntilVote;
		public int numVotes;
	}
	
	static public class PlaylistTrack {
		PlaylistTrack() {}
		public String artist;
		public String title;
		public String album;
		public int length;
		public String uri;
	}
}
