package com.example.liam.flashbackplayer;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;


public class Song implements Comparable {

    // initialize the preference to neutural
    public static final int DISLIKE = -1;
    public static final int NEUTRAL = 0;
    public static final int FAVORITE = 1;

    private String name;
    private String fileName;
    private String artist;
    private String albumName;
    private int length; //length in milliseconds

    private int[] timePeriod; // period of a day, declared to be an array of size 3
    //5am-10:59am, 11am-4:49pm, and 5pm-4:49am

    private int[] day;  // day of a week, declared to be an array of size 7

    private long lastPlayTime;    //last time to play the song
    //for example: 41726.0 means Thrusday, 17:26
    // so the larger nunmber is always the most current played song

    private ArrayList<SongLocation> locations;

    private int preference;

    private int ranking;

    public Song(String name, String fileName, String artist, int length, String albumName) {
        this.name = name;
        this.fileName = fileName;
        this.artist = artist;
        this.length = length;
        this.albumName = albumName;

        this.timePeriod = new int[3];
        this.day = new int[7];
        this.locations = new ArrayList<SongLocation>();
        this.preference = NEUTRAL;
        this.ranking = 0;
    }

    public void updateMetadata(SongLocation loc, int dayOfweek, int hour, long lastPlayTime) {
        // set the day of week to be true
        day[dayOfweek - 1] = 1;

        //set the period of a day
        if (5 <= hour && hour < 11)
            timePeriod[0]++;
        else if (11 <= hour && hour < 16)
            timePeriod[1]++;
        else
            timePeriod[2]++;

        this.lastPlayTime = lastPlayTime;
        this.locations.add(loc);
    }

    public void setPreference(int pref) {
        this.preference = pref;
    }

    public void increaseRanking() {
        this.ranking++;
    }

    public int getRanking() {
        return ranking;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }

    public String getArtist() {
        return artist;
    }

    public int getLength() {
        return length;
    }

    public String getAlbumName() {
        return albumName;
    }


    public int[] getDay() {
        return day;
    }

    public int[] getTimePeriod() {
        return timePeriod;
    }

    public ArrayList<SongLocation> getLocations() {
        return locations;
    }

    public int getPreference() {
        return preference;
    }

    public long getLastPlayTime() {
        return lastPlayTime;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        Song other = (Song) o;
        return this.name.compareTo(other.getName());
    }
}

