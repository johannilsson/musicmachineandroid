/*
 * Copyright (C) 2009 Johan Nilsson <http://markupartist.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.markupartist.musicmachine.gateway;

import android.util.Log;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.ArrayList;

public class SpotifyGatewayTrackParser extends DefaultHandler {
    private static final String TAG = "SpotifyGatewayTrackParser";
    private static final String INCLUDED_COUNTRY_CODE = "SE";

    private StringBuilder mTextBuffer = null;
    boolean mIsBuffering = false;
    private ArrayList<SpotifyGatewayTrack> mTracks = new ArrayList<SpotifyGatewayTrack>();
    private SpotifyGatewayTrack mCurrentTrack;
    private boolean isInArtist = false;
    private boolean isInAlbum = false;
    private boolean isInTrack = false;
    private boolean includeTrack = false;

    public ArrayList<SpotifyGatewayTrack> parseTracks(InputSource input) throws SpotifyGatewayParseException {
        mTracks.clear();
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            xr.setContentHandler(this);
            input.setEncoding("UTF-8");
            xr.parse(input);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            throw new SpotifyGatewayParseException();
        } catch (SAXException e) {
            Log.e(TAG, e.toString());
            throw new SpotifyGatewayParseException();
        } catch (ParserConfigurationException e) {
            Log.e(TAG, e.toString());
            throw new SpotifyGatewayParseException();
        }

        return mTracks;
    }

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
    public void startElement(String uri, String name, String qName, Attributes atts) {
        if (name.equals("track")) {
            isInTrack = true;
            mCurrentTrack = new SpotifyGatewayTrack();
            mCurrentTrack.setUri(atts.getValue("href"));
        } else if (name.equals("artist")) {
            isInArtist = true;
        } else if (name.equals("album")) {
            isInAlbum = true;
        } else if (name.equals("territories")) {
            startBuffer();
        } else if (name.equals("name") && isInArtist) {
            startBuffer();
        } else if (name.equals("name") && isInAlbum) {
            startBuffer();
        } else if (name.equals("name") && isInTrack) {
            startBuffer();
        }
    }

    public void characters(char ch[], int start, int length) {
        if (mIsBuffering) {
            mTextBuffer.append(ch, start, length);
        }
    }

    public void endElement(String uri, String name, String qName)
                throws SAXException {
        if (name.equals("track")) {
            isInTrack = false;
            if(includeTrack) {
                mTracks.add(mCurrentTrack);
            }
            includeTrack = false;
        } else if (name.equals("artist")) {
            isInArtist = false;
        } else if (name.equals("album")) {
            isInAlbum = false;
        } else if (name.equals("territories")) {
            endBuffer();
            if(mTextBuffer.indexOf(INCLUDED_COUNTRY_CODE) != -1) {
                includeTrack = true;
            }
        } else if (name.equals("name") && isInArtist) {
            endBuffer();
            mCurrentTrack.setArtist(mTextBuffer.toString());
        } else if (name.equals("name") && isInAlbum) {
            endBuffer();
            mCurrentTrack.setAlbum(mTextBuffer.toString());
        } else if (name.equals("name") && isInTrack) {
            endBuffer();
            mCurrentTrack.setTitle(mTextBuffer.toString());
        }
    }

    private void startBuffer() {
        mTextBuffer = new StringBuilder();
        mIsBuffering = true;
    }

    private void endBuffer() {
        mIsBuffering = false;
    }

    public class SpotifyGatewayParseException extends Exception {}
}
