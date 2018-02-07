package com.example.liam.flashbackplayer;

import java.util.ArrayList;

/**
 * Created by liam on 2/6/18.
 */

public class Album {
    private String name;
    private ArrayList<Song> songList;
    private String year;

    public Album(String name, String year) {
        this.name = name;
        this.year = year;
        this.songList = new ArrayList<Song>();
    }

    public void addSong(Song song) {
        this.songList.add(song);
    }

    public ArrayList<Song> getSongList() {
        return this.songList;
    }
}
