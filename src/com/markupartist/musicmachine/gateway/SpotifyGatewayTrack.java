package com.markupartist.musicmachine.gateway;

/**
 * Created by IntelliJ IDEA.
 * User: joakimb
 * Date: May 1, 2010
 * Time: 2:20:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class SpotifyGatewayTrack {
    private String artist;
    private String title;
    private String album;
    private String uri;
    private double length;

    public SpotifyGatewayTrack()
    {
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }

    public String getUri() {
        return uri;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return "Track{" +
                "artist='" + artist + '\'' +
                ", title='" + title + '\'' +
                ", album='" + album + '\'' +
                ", uri='" + uri + '\'' +
                '}';
    }
}
