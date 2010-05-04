package com.markupartist.musicmachine.gateway;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

/**
 * @author marco
 * LastFM album parser. Parses the minimal information needed to extract album
 * art and being debug friendly.
 */
public class LastFMGatewayAlbumParser extends DefaultHandler {
	private static String TAG = LastFMGatewayAlbumParser.class.getSimpleName();
	private ArrayList<LastFMGatewayAlbum> albums = new ArrayList<LastFMGatewayAlbum>();
	private LastFMGatewayAlbum currentAlbum;
	private StringBuilder textBuffer;
	//private boolean isInArtist;
	//private boolean is
	private boolean isBuffering;
	private boolean isInImage;
	
    public ArrayList<LastFMGatewayAlbum> parseAlbums(InputSource input) throws LastFMGatewayParseException {
    	albums.clear();
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            xr.setContentHandler(this);
            input.setEncoding("UTF-8");
            xr.parse(input);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            throw new LastFMGatewayParseException();
        } catch (SAXException e) {
            Log.e(TAG, e.toString());
            throw new LastFMGatewayParseException();
        } catch (ParserConfigurationException e) {
            Log.e(TAG, e.toString());
            throw new LastFMGatewayParseException();
        }

        return albums;
    }
    
    public void startElement(String uri, String name, String qName, Attributes atts) {
    	if (name.equals("album")) {
    		currentAlbum = new LastFMGatewayAlbum();
    	} else if(name.equals("artist")) {
    		startBuffer();
    	} else if(name.equals("name")) {
    		startBuffer();
        } else if (name.equals("image")) {
        	// Try the largest image first
        	if(atts.getValue("size").equals("extralarge")) {
        		isInImage = true;
        		startBuffer();
        	} else if(atts.getValue("size").equals("large") && currentAlbum.coverURL == null){
        		isInImage = false;
        		startBuffer();
        	}
        }
    }

    public void characters(char ch[], int start, int length) {
        if (isBuffering) {
            textBuffer.append(ch, start, length);
        }
    }

    public void endElement(String uri, String name, String qName) throws SAXException {
    	if(name.equals("album")) {
    		albums.add(currentAlbum);
    	} else if(name.equals("name")) {
    		endBuffer();
    		currentAlbum.album = textBuffer.toString();
    	} else if(name.equals("artist")) {
    		endBuffer();
    		currentAlbum.artist = textBuffer.toString();
    	} else if(name.equals("image") && true == isInImage) {
    		isInImage = false;
    		endBuffer();
    		currentAlbum.coverURL = textBuffer.toString();
        }
    }

    private void startBuffer() {
        textBuffer = new StringBuilder();
        isBuffering = true;
    }

    private void endBuffer() {
        isBuffering = false;
    }
    
    public class LastFMGatewayParseException extends Exception {}

}
