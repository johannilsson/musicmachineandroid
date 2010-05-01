package com.markupartist.musicmachine.gateway;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.markupartist.musicmachine.utils.HttpManager;

/*
 * Copyright: ANARCHY! YEAH!
 */
public class MusicMachineGateway {
	private String serverHost = "10.0.2.1";
	private int serverPort = 8080;
	Gson gson = new Gson();
	
	public MusicMachineGateway() {
		
	}

	public MusicMachineGateway(String serverHost, int serverPort) {
		this.serverHost = serverHost;
		this.serverPort = serverPort;
	}

	private String getUrl(String path) {
		return String.format("http://%s:%s%s", serverHost, serverPort, path);
	}
	
	private String getRequest(String path) throws IOException {
		HttpGet get = new HttpGet(getUrl(path));
		HttpResponse r = HttpManager.execute(get);
        if (r.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new IOException("A remote server error occurred");
        }
        return this.convertStreamToString(r.getEntity().getContent());
	}
	
	private String postRequest(String path, String data) throws IOException, ConflictException {
		HttpPost post = new HttpPost(getUrl(path));
		post.setEntity(new StringEntity(data));
		HttpResponse r = HttpManager.execute(post);
        if (r.getStatusLine().getStatusCode() == 409) {
        	throw new ConflictException();
        }
        else if (r.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new IOException("A remote server error occurred");
        }
        return this.convertStreamToString(r.getEntity().getContent());
	}

	public Status getStatus() throws IOException {
		Status s = gson.fromJson(getRequest("/status"), Status.class);
		return s;
	}
	
	private List<PlaylistTrack> _getPlaylist(boolean played) throws IOException {
		Type collectionType = new TypeToken<List<PlaylistTrack>>(){}.getType();
		List<PlaylistTrack> tracks;
		String params = "";
		if (played) {
			params = "p=played";
		}
		try {
			tracks = gson.fromJson(getRequest(String.format("/playlist?%s", params)), collectionType);
		} catch (JsonParseException e) {
			throw new IOException(e.getMessage());
		}
		return tracks;
	}

	public List<PlaylistTrack> getPlaylist() throws IOException {
		return _getPlaylist(false);
	}
	public List<PlaylistTrack> getPreviousTracks() throws IOException {
		return _getPlaylist(true);
	}
	
	public void vote(String trackUri, String userId) throws IOException, UserHasAlreadyVotedException {
		vote(new Vote(trackUri, userId));
	}
	
	public void vote(Vote vote) throws IOException, UserHasAlreadyVotedException {
		try {
			postRequest("/vote", gson.toJson(vote));
		} catch (ConflictException e) {
			throw new UserHasAlreadyVotedException();
		}
	}
	
	private String convertStreamToString(InputStream is) throws IOException {
        /*
         * Bla bla bla stolen from somewhere, bla bla
         * 
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } finally {
                is.close();
            }
            return sb.toString();
        } else {        
            return "";
        }
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
	
	static public class Vote {
		Vote() {};
		Vote(String track, String user) {
			this.track = track;
			this.user = user;
		}
		public String track;
		public String user;
	}
	
	static public class ConflictException extends Exception {}
	static public class UserHasAlreadyVotedException extends Exception {}
}
