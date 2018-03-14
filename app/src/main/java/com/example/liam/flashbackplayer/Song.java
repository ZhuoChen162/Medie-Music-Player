package com.example.liam.flashbackplayer;

import android.media.MediaPlayer;
import android.support.annotation.NonNull;

import java.util.ArrayList;


public interface Song extends Comparable {
    // initialize the preference to neutural
    int DISLIKE = 1;
    int NEUTRAL = 0;
    int FAVORITE = 2;

    void updateMetadata(SongLocation loc, int dayOfweek, int hour, long lastPlayTime);

    void setPreference(int pref);

    void increaseRanking();

    int getRanking();

    void setRanking(int ranking);

    String getName();

    String getSource();

    String getArtist();

    int getLength();

    int[] getTimePeriod();

    int[] getDay();

    String getAlbumName();

    ArrayList<SongLocation> getLocations();

    int getPreference();

    void changePreference();

    long getLastPlayTime();

    void play(MediaPlayer mediaPlayer);

    String getId();

    String getUrl();

    @Override
    int compareTo(@NonNull Object o);
}
