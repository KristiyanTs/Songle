package com.moonhythe.songle.Downloader;

import android.os.AsyncTask;
import android.util.Log;

import com.moonhythe.songle.Parser.SongParser;
import com.moonhythe.songle.Structure.Song;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by kris on 03/11/17.
 * Downloads a previously predefined song chosen at random
 */

public class DownloadSong extends AsyncTask<String, Void, Song> {

    private static final String TAG = DownloadSong.class.getSimpleName();

    String baseUrl = "http://www.inf.ed.ac.uk/teaching/courses/selp/data/songs/songs.xml";

    @Override
    protected Song doInBackground(String... urls) {
        Song fail = null;
        try {
            return loadXmlFromNetwork(urls[0]);
        } catch (IOException e) {
            Log.i(TAG, "IOException at loadingXmlFromNetwork");
            return fail;   // TO DO handle exception
        } catch (XmlPullParserException e) {
            Log.i(TAG, "XMLPullParserException at loadingXml");
            return fail;   // TO DO handle exception
        }
    }

    private Song loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        SongParser xmlParser = new SongParser();
        Song song = null;
        String number = null;
        String artist = null;
        String title = null;
        String link = null;
        Log.i(TAG, "Begin loadXmlFromNetwork");

        try {
            stream = downloadUrl(urlString);
            song = xmlParser.parse(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        Log.i(TAG, "Song downloaded and parsed");
        return song;
    }

    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }
}
