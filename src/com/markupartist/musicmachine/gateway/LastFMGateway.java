package com.markupartist.musicmachine.gateway;

import java.io.IOException;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.xml.sax.InputSource;

import com.markupartist.musicmachine.gateway.LastFMGatewayAlbumParser.LastFMGatewayParseException;
import com.markupartist.musicmachine.utils.HttpManager;

import android.text.TextUtils;
import android.util.Log;

public class LastFMGateway {
	private static String TAG = LastFMGateway.class.getSimpleName();
	private static String KEY = "1179cb08e0682a70d291692c5ef17201";
	private static String BASE_URL = "http://ws.audioscrobbler.com/2.0/?api_key=" + KEY;
	
	public List<LastFMGatewayAlbum> searchAlbum(String artist, String album) throws LastFMGatewayException, LastFMGatewayParseException{	

		StringBuilder builder = new StringBuilder(BASE_URL);
		builder.append("&method=album.getinfo");
		if(null != artist && false == TextUtils.isEmpty(artist)) {
			builder.append("&artist=").append(URLEncoder.encode(artist));
		}
		
		if(null != album && false == TextUtils.isEmpty(album)) {
			builder.append("&album=").append(URLEncoder.encode(album));
		}
		
		Log.d(TAG, builder.toString());
        HttpGet searchHttpGet = new HttpGet(builder.toString());
        
        HttpResponse response = null;
        try {
            response = HttpManager.execute(searchHttpGet);
        } catch (IOException e) {
            Log.e(TAG, "HttpManager.execute", e);
            throw new LastFMGatewayException();
        }

        InputStream responseContentStream = null;
        try {
            responseContentStream = response.getEntity().getContent();
        } catch (IOException e) {
            Log.e(TAG, "getContent", e);
            throw new LastFMGatewayException();
        }

        List<LastFMGatewayAlbum> result = new LastFMGatewayAlbumParser().parseAlbums(new InputSource((responseContentStream)));
		return result;
	}
	
	public class LastFMGatewayException extends Exception {}
}
