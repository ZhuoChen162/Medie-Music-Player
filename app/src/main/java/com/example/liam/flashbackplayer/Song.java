package com.example.liam.flashbackplayer;

import android.media.MediaPlayer;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Comparator;


public interface Song extends Comparable {
    // initialize the preference to neutural

    // CHANGED neutrual from 0 to 1 and chagne Dislike from 1 to 0
    int DISLIKE = 0;
    int NEUTRAL = 1;
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

    /*Comparator for sorting the list by Artist*/
    Comparator<Song> SongArtistCompartor = new Comparator<Song>() {

        public int compare(Song s1, Song s2) {
            String Artist1 = s1.getArtist().toUpperCase();
            String Artist2 = s2.getArtist().toUpperCase();

            //ascending order
            return Artist1.compareTo(Artist2);
        }};

    /*Comparator for sorting the list by favorite*/
    Comparator<Song> SongFavoriteCompartor = new Comparator<Song>() {

        public int compare(Song s1, Song s2) {

            Integer Favorite1 = s1.getPreference();
            Integer Favorite2 = s2.getPreference();

            //descending order
            return (-1*Favorite1.compareTo(Favorite2));
        }};
}
