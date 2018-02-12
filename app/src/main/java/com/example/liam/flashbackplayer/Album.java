package com.example.liam.flashbackplayer;

import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by liam on 2/6/18.
 */

public class Album implements Comparable{
    private String name;
    private ArrayList<Song> songList;

    public Album(String name) {
        this.name = name;
        this.songList = new ArrayList<Song>();
    }

    public void addSong(Song song) {
        this.songList.add(song);
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<Song> getSongList() {
        return this.songList;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        Album other = (Album)o;
        return this.name.compareTo(other.getName());
    }
}
