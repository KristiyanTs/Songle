package com.moonhythe.songle.Parser;

import android.content.Context;
import android.util.Xml;

import com.moonhythe.songle.GameManager.GameData;
import com.moonhythe.songle.Structure.Preference;
import com.moonhythe.songle.Structure.Song;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by kris on 03/11/17.
 * Parses the info for a specified by the constructor song
 */

public class SongParser {

    private static final String TAG = SongParser.class.getSimpleName();
    private static final String ns = null;
    private Context context;
    private GameData dataManager;

    public SongParser(Context context, GameData dataManager) {
        this.context = context;
        this.dataManager = dataManager;
    }

    public Song parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private Song readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        Song song;
        List<Song> album = new ArrayList<Song>();

        // Iterate songs and add them to a list
        parser.require(XmlPullParser.START_TAG, ns, "Songs");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Song")) {
                song = readSong(parser);
                album.add(song);
            } else {
                skip(parser);
            }
        }

        if(dataManager.getContinue_game()){  // If continue game, get last played song
            return album.get(Integer.parseInt(Preference.getSharedPreferenceString(context, "song_number", "01"))-1);
        } else{                              // If new game, pick a random song
            String removed_songs_string = Preference.getSharedPreferenceString(context, "removed_songs", "");
            if(removed_songs_string.length() != 0){
                album = filterPlayableSongs(removed_songs_string, album);
            }

            // picking a random song and returning it
            Random rand = new Random();
            return album.get(rand.nextInt(10000)%album.size());
        }
    }


    public List<Song> filterPlayableSongs(String removed, List<Song> album){
        String[] removed_songs = removed.split(" ");

        for(String r : removed_songs){
            for(Song s : album){
                if(s.getNumber().equals(r)){
                    album.remove(s);
                    break;
                }
            }
        }
        return album;
    }

    private Song readSong(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "Song");
        Song song = new Song();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tag_name = parser.getName();
            switch (tag_name) {
                case "Number":
                    song.setNumber(readXml(parser, "Number"));
                    break;
                case "Artist":
                    song.setArtist(readXml(parser, "Artist"));
                    break;
                case "Title":
                    song.setTitle(readXml(parser, "Title"));
                    break;
                case "Link":
                    song.setLink(readXml(parser, "Link"));
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        return song;
    }

    private String readXml(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, tag);
        String str = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, tag);
        return str;
    }

    private String readText(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
