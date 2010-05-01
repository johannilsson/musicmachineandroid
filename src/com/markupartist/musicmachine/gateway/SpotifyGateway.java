package com.markupartist.musicmachine.gateway;

import android.util.Log;
import com.markupartist.musicmachine.utils.HttpManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: joakimb
 * Date: May 1, 2010
 * Time: 10:20:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class SpotifyGateway {
    private static String TAG = "SpotifyGateway";

    public List<SpotifyGatewayTrack> searchTrack(String searchTerm) throws SpotifyGatewayTrackParser.SpotifyGatewayParseException, SpotifyGatewayException {
        List<SpotifyGatewayTrack> searchResult;

        HttpGet searchHttpGet = new HttpGet("http://ws.spotify.com/search/1/track.xml?q=" + URLEncoder.encode(searchTerm));

        HttpResponse response = null;
        try {
            response = HttpManager.execute(searchHttpGet);
        } catch (IOException e) {
            Log.e(TAG, "HttpManager.execute", e);
            throw new SpotifyGatewayException();
        }

        InputStream responseContentStream = null;
        try {
            responseContentStream = response.getEntity().getContent();
        } catch (IOException e) {
            Log.e(TAG, "getContent", e);
            throw new SpotifyGatewayException();
        }

        SpotifyGatewayTrackParser parser = new SpotifyGatewayTrackParser();
        searchResult = parser.parseTracks(new InputSource(responseContentStream));

        return searchResult;
    }

    public SpotifyGatewayTrack lookup(String spotifyUri) throws SpotifyGatewayException, SpotifyGatewayTrackParser.SpotifyGatewayParseException {
        List<SpotifyGatewayTrack> searchResult;
        HttpGet searchHttpGet = new HttpGet("http://ws.spotify.com/lookup/1/?uri=" + URLEncoder.encode(spotifyUri));

        HttpResponse response = null;
        try {
            response = HttpManager.execute(searchHttpGet);
        } catch (IOException e) {
            Log.e(TAG, "HttpManager.execute", e);
            throw new SpotifyGatewayException();
        }

        InputStream responseContentStream = null;
        try {
            responseContentStream = response.getEntity().getContent();
        } catch (IOException e) {
            Log.e(TAG, "getContent", e);
            throw new SpotifyGatewayException();
        }

        SpotifyGatewayTrackParser parser = new SpotifyGatewayTrackParser();
        searchResult = parser.parseTracks(new InputSource(responseContentStream));


        if(searchResult.size() > 0)
        {
            return searchResult.get(0);
        } else {
            throw new SpotifyGatewayException();
        }
    }

    public class SpotifyGatewayException extends Exception {}
}
