package com.markupartist.musicmachine.gateway;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: joakimb
 * Date: May 1, 2010
 * Time: 10:20:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class SpotifyGateway {
    public List<Track> searchTrack(String searchTerm)
    {
        List<Track> searchResult = new ArrayList<Track>();

        SpotifyGateway.Track mockedTrack = new SpotifyGateway.Track("Kaizers Orchestra", "Prosessen", "Våre Demoner", "spotify:title:5S9edTU6BpUmp0enwiYoZ6");
        searchResult.add(mockedTrack);

        return searchResult;

        /*

        HttpHost searchHttpHost = new HttpHost("ws.spotify.com", 80);

        URI searchUrl = null;
        try {
            searchUrl = new URI("http", "", "ws.spotify.com", 80, "/search/1/title.xml", "q=" + searchTerm, "");
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        HttpGet searchHttpGet = new HttpGet(searchUrl);

        HttpResponse response = null;
        try {
            response = HttpManager.execute(searchHttpGet);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        InputStream responseContentStream = null;
        try {
            responseContentStream = response.getEntity().getContent();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        SAXReader reader = new SAXReader(); // dom4j SAXReader
        Document document = null; // dom4j Document
        try {
            document = reader.read(responseContentStream);
        } catch (DocumentException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        */

        /*
          <track xmlns="http://www.spotify.com/ns/music/1" href="spotify:title:5S9edTU6BpUmp0enwiYoZ6">
            <name>Prosessen</name>
            <artist href="spotify:artist:1s1DnVoBDfp3jxjjew8cBR">
              <name>Kaizers Orchestra</name>
            </artist>
            <id type="isrc">NOHDL0901100</id>
            <album href="spotify:album:6CSTOAMHphZGon9leEyN9c">
              <name>Våre Demoner</name>
              <released>2009</released>
              <availability>
                <territories>AD AE AF AG AI AL AM AN AO AQ AR AS AT AU AW AX AZ BA BB BD BE BF BG BH BI BJ BM BN BO BR BS BT BV BW BY BZ CA CC CD CF CG CH CI CK CL CM CN CO CR CU CV CX CY CZ DE DJ DK DM DO DZ EC EE EG EH ER ES ET FI FJ FK FM FO FR GA GB GD GE GF GG GH GI GL GM GN GP GQ GR GS GT GU GW GY HK HM HN HR HT HU ID IE IL IN IO IQ IR IS IT JM JO JP KE KG KH KI KM KN KP KR KW KY KZ LA LB LC LI LK LR LS LT LU LV LY MA MC MD ME MG MH MK ML MM MN MO MP MQ MR MS MT MU MV MW MX MY MZ NA NC NE NF NG NI NL NO NP NR NU NZ OM PA PE PF PG PH PK PL PM PN PR PS PT PW PY QA RE RO RS RU RW SA SB SC SD SE SG SH SI SJ SK SL SM SN SO SR ST SV SY SZ TC TD TF TG TH TJ TK TL TM TN TO TR TT TV TW TZ UA UG UM US UY UZ VA VC VE VG VI VN VU WF WS YE YT ZA ZM ZW</territories>
              </availability>
            </album>
            <track-number>10</track-number>
            <length>250.280000</length>
            <popularity>0.73049</popularity>
          </track>
        */

        /*

        List<Node> trackNodes = document.selectNodes("//tracks/track");
        for (Node trackNode : trackNodes)
        {
            SpotifyGateway.Track currenTrack = new SpotifyGateway.Track();

            Node artistNode = trackNode.selectSingleNode("artist/name");
            currenTrack.setArtist(artistNode.getText());

            Node albumNode = trackNode.selectSingleNode("album/name");
            currenTrack.setAlbum(albumNode.getText());

            Node trackNameNode = trackNode.selectSingleNode("name");
            currenTrack.setTitle(trackNameNode.getText());

            currenTrack.setUri(trackNode.valueOf("@href"));
        }

        return searchResult;
        */
    }

    public class Track {
        private String artist;
        private String title;
        private String album;
        private String uri;

        public Track()
        {
        }

        public Track(String artistName, String title, String albumName, String uri)
        {
            this.artist = artistName;
            this.title = title;
            this.album = albumName;
            this.uri = uri;
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
}
