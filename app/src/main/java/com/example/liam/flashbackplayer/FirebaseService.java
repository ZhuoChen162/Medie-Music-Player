package com.example.liam.flashbackplayer;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class FirebaseService {
    private FirebaseDatabase database;
    private UrlList urlList;

    public FirebaseService(UrlList urlList) {
        database = FirebaseDatabase.getInstance();
        this.urlList = urlList;
    }

    public void makeCloudChangelist(final Map<String, String> localSongList, final Map<String, String> changeList) {
        DatabaseReference cloudListRef = database.getReference("songs");
        cloudListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot song : dataSnapshot.getChildren()) {
                    if (!localSongList.containsKey(song.getKey())) {
                        changeList.put(song.getKey(), song.getValue(String.class));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Failed to read value.", databaseError.toException());
            }
        });
    }

    public void makePlayList(final ArrayList<Song> songList, final HashMap<String, String> friends, final int curYearAndDay, final double curLon, final double curLat) {
        DatabaseReference cloudHistListRef = database.getReference("songsInfo");
        cloudHistListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (Song curSong : songList) {
                    DataSnapshot curSongHist = dataSnapshot.child(curSong.getId());
                    if (curSongHist == null) {
                        curSong.setRanking(0);
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

    public void uploadSongs() {
        updateCloudSongList(urlList.getLocalChange());
    }

}
