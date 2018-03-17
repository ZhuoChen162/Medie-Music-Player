package com.example.liam.flashbackplayer;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class FirebaseService {
    private FirebaseDatabase database;
    protected MusicLoader loader;
    private MediaMetadataRetriever mmr = new MediaMetadataRetriever();
    private FlashbackManager flashbackManager;
    private UrlList urlList;

    public FirebaseService(UrlList urlList, MusicLoader loader, FlashbackManager flashbackManager) {
        database = FirebaseDatabase.getInstance();
        this.urlList = urlList;
        this.loader = loader;
        this.flashbackManager = flashbackManager;
    }

    // Get the songs that exist only on the cloud
    public void makeCloudChangelist(final Map<String, String> localSongList) {

        database.getReference("songs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> changeList = new HashMap<>();

                for (DataSnapshot song : dataSnapshot.getChildren()) {
                    if (!localSongList.containsKey(song.getKey())) {
                        changeList.put(song.getKey(), song.getValue(String.class));
                    }
                }

                for (Map.Entry<String, String> pair : changeList.entrySet()) {
                    String songId = pair.getKey();
                    String[] parts = songId.split("=");
                    Song downloading = new LocalSong(parts[0], parts[1], songId, pair.getValue());
                    MainActivity.masterList.add(downloading);
                    loader.addSongToAlbum(downloading);

                    new DownloadSongAsync().execute(downloading);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Failed to read songs.", databaseError.toException());
            }
        });
    }

    public void makePlayList(final UIManager uiManager, final ArrayList<Song> songList, final HashMap<String, String> friends, final int curYearAndDay, final double curLon, final double curLat) {
        DatabaseReference cloudHistListRef = database.getReference("songsInfo");
        cloudHistListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (Song curSong : songList) {
                    DataSnapshot curSongHist = dataSnapshot.child(curSong.getId());
                    if (curSongHist == null) {
                        curSong.setRanking(0);
                        curSong.setPlayedBy("");
                        continue;
                    }

                    int maxRank = 0;
                    String maxUser = "";
                    for (DataSnapshot oneHist : curSongHist.getChildren()) {
                        int curRank = 0;
                        double lat = oneHist.child("lat").getValue(Double.class);
                        double lon = oneHist.child("lon").getValue(Double.class);
                        int yearAndDay = oneHist.child("day").getValue(Integer.class);
                        String userId = oneHist.child("user").getValue(String.class);

                        // (a) whether it was played near the user's present location
                        if ((Math.pow(curLat - lat, 2) + Math.pow(curLon - lon, 2)) < 0.0001)
                            curRank += 1000;

                        // (b) whether it was played in the last week
                        if (curYearAndDay % 1000 < 8) {
                            if (curYearAndDay - yearAndDay < 648) curRank += 100;
                        } else {
                            if (curYearAndDay - yearAndDay < 8) curRank += 100;
                        }

                        // (c) whether it was played by a friend
                        if (friends.containsKey(userId)) curRank += 10;

                        if (curRank == 1000) curRank = 105;

                        if (curRank > maxRank) {
                            maxRank = curRank;
                            maxUser = userId;
                        }
                    }
                    curSong.setRanking(maxRank);
                    curSong.setPlayedBy(maxUser);
                }

                for (Song song : songList)
                    Log.d("RANKINGGGGGGGGGG", String.valueOf(song.getRanking()));

                flashbackManager.rankSongs(songList);
                PriorityQueue<Song> pq = flashbackManager.getRankList();

                //add songs in pq into the flashbackList
                while (!pq.isEmpty())
                    MainActivity.flashbackList.add(pq.poll());
                uiManager.populateUI(MainActivity.MODE_VIBE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void updateCloudSongList(Map<String, String> songList) {
        Map<String, Object> objList = new HashMap<>();
        objList.putAll(songList);
        DatabaseReference cloudListRef = database.getReference("songs");
        cloudListRef.updateChildren(objList);
    }

    public void uploadPlayInfo(String songId, SongLocation loc, int yearAndDay, String userId) {
        DatabaseReference songHist = database.getReference("songsInfo/" + songId);
        DatabaseReference newHist = songHist.push();
        newHist.child("lat").setValue(loc.latitude);
        newHist.child("lon").setValue(loc.longitude);
        newHist.child("day").setValue(yearAndDay);
        newHist.child("user").setValue(userId);
    }

    class DownloadSongAsync extends AsyncTask<Song, Void, String> {

        @Override
        protected String doInBackground(Song... songs) {
            Song downloading = songs[0];
            String fileUrl = downloading.getUrl();
            Log.i("DownloadStarted", downloading.getName());

            InputStream input = null;
            FileOutputStream output = null;
            HttpURLConnection urlConnection = null;
            File dest = loader.getDefaultMusicDirectory();
            try {
                URL url = new URL(fileUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report instead of the file
                if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + urlConnection.getResponseCode()
                            + " " + urlConnection.getResponseMessage();
                }

                String fileName = "";
                String disposition = urlConnection.getHeaderField("Content-Disposition");
                String contentType = urlConnection.getContentType();
                int contentLength = urlConnection.getContentLength();

                if (disposition != null) {
                    // extracts file name from header field
                    int index = disposition.indexOf("filename=");
                    if (index > 0) {
                        fileName = disposition.substring(index + 10, disposition.length() - 1);
                        if (fileName.contains("\"")) {
                            fileName = fileName.split("\"")[0];
                        }
                    }
                } else {
                    // extracts file name from URL
                    fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1, fileUrl.length());
                }
                System.out.println("Content-Type = " + contentType);
                System.out.println("Content-Disposition = " + disposition);
                System.out.println("Content-Length = " + contentLength);
                System.out.println("fileName = " + fileName);

                String path = dest.toString() + File.separator + fileName;

                // download the file
                input = urlConnection.getInputStream();

                // opens an output stream to save into file
                output = new FileOutputStream(path);

                byte data[] = new byte[4096];
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    output.write(data, 0, count);
                }

                Log.i("DownloadFinished", downloading.getName());
                Log.i("DownloadFinishedPath", path);

                FileDescriptor fd = output.getFD();
                mmr.setDataSource(fd);
                String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String length = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                Log.i("DownloadFinishedArtist", artist);

                downloading.setArtist(artist == null ? "Unknown Artist" : artist);
                downloading.setLength(length == null ? 0 : Integer.parseInt(length));
                downloading.setSource(path);

            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (urlConnection != null)
                    urlConnection.disconnect();
            }

            return null;
        }
    }
}
