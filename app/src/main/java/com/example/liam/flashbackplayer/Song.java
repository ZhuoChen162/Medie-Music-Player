package com.example.liam.flashbackplayer;

import android.location.Location;
import android.support.annotation.NonNull;

import java.util.ArrayList;


public class Song implements Comparable {
    public static final int DISLIKE = 0;
    public static final int NEUTRAL = 1;
    public static final int FAVORITE = 2;
    private String name;
    private String fileName;
    private String artist;
    private String albumName;
    private int length; //length in milliseconds
    private int[] times;
    private int[] day;
    private double lastPlayTime;
    private ArrayList<Location> locations;
    private int preference;

    public Song(String name, String fileName, String artist, int length, String albumName) {
        this.name = name;
        this.fileName = fileName;
        this.artist = artist;
        this.length = length;
        this.albumName = albumName;
        this.times = new int[3];
        this.day = new int[7];
        this.locations = new ArrayList<Location>();
        this.preference = NEUTRAL;
    }

    public void updateMetadata(Location loc, int time) {
        this.locations.add(loc);
//        this.times.add(time);
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

    public int[] getTimes() {
        return times;
    }

    public ArrayList<Location> getLocations() {
        return locations;
    }

    public void setPreference(int pref) {
        this.preference = pref;
    }

    public int getPreference() {
        return preference;
    }


    @Override
    public int compareTo(@NonNull Object o) {
        Song other = (Song) o;
        return this.name.compareTo(other.getName());
    }
}

