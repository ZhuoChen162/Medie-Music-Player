package com.example.liam.flashbackplayer;

import android.support.annotation.NonNull;

import java.util.ArrayList;


public class Album implements Comparable{
    private String name;
    private ArrayList<Song> songList;
    private ArrayList<String> cacheCheck;

    public Album(String name) {
        this.name = name;
        this.songList = new ArrayList<Song>();
        this.cacheCheck = new ArrayList<String>();
    }

    public void addSong(Song song) {
        this.songList.add(song);
        this.cacheCheck.add(song.getName());
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<Song> getSongList() {
        return this.songList;
    }

    public boolean contains(String songName) {
        if(this.cacheCheck.contains(songName)) {
            return true;
        } else {
//            this.cacheCheck.add(songName);
            return false;
        }
    }

    @Override
    public int compareTo(@NonNull Object o) {
        Album other = (Album)o;
        return this.name.compareTo(other.getName());
    }
}
