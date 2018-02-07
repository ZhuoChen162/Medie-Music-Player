package com.example.liam.flashbackplayer;

import java.util.ArrayList;

/**
 * Created by liam on 2/6/18.
 */

public class Song {
    private String name;
    private String fileName;
    private String artist;
    //private String album;
    private int length; //length in milliseconds
    private ArrayList<Integer> times;
    private ArrayList<String> locations;

    public Song(String name, String fileName, String artist, int length) {
        this.name = name;
        this.fileName = fileName;
        this.artist = artist;
        this.length = length;
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

    public ArrayList<Integer> getTimes() {
        return times;
    }

    public ArrayList<String> getLocations() {
        return locations;
    }

}

