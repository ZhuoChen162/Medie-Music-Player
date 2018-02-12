package com.example.liam.flashbackplayer;

import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by liam on 2/6/18.
 */

public class Song implements Comparable{
    private String name;
    private String fileName;
    private String artist;
    private String albumName;
    private int length; //length in milliseconds
    private ArrayList<Integer> times;
    private ArrayList<String> locations;

    public Song(String name, String fileName, String artist, int length, String albumName) {
        this.name = name;
        this.fileName = fileName;
        this.artist = artist;
        this.length = length;
        this.albumName = albumName;
        this.times = new ArrayList<Integer>();
        this.locations = new ArrayList<String>();
    }

    public void updateMetadata(String loc, int time) {
        this.locations.add(loc);
        this.times.add(time);
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

    public ArrayList<Integer> getTimes() {
        return times;
    }

    public ArrayList<String> getLocations() {
        return locations;
    }


    @Override
    public int compareTo(@NonNull Object o) {
        Song other = (Song)o;
        return this.name.compareTo(other.getName());
    }
}

