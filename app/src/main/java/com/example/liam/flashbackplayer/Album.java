package com.example.liam.flashbackplayer;

import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * This is a album class that allow to store and build a alum and able to
 * add song to song list and check if the song are exist or not
 */
public class Album implements Comparable{

    // private variables that store song to a arraylist.
    private String name;
    private ArrayList<Song> songList;
    private ArrayList<String> cacheCheck;

    /**
     * constructor that build a new album with that name and initilize the two arraylist to
     * store information about the song
     * @param name  album name that want to build a album
     */
    public Album(String name) {
        this.name = name;
        this.songList = new ArrayList<Song>();
        this.cacheCheck = new ArrayList<String>();
    }

    /**
     * Method that allow to add song to the album
     * @param song song that want to add
     */
    public void addSong(Song song) {
        this.songList.add(song);
        this.cacheCheck.add(song.getName());
    }

    public void removeSong(Song song) {
        this.songList.remove(song);
        this.cacheCheck.remove(song.getName());
    }

    /**
     * This method will simply return the album name
     *
     * @return name of the album
     */
    public String getName() {
        return this.name;
    }

    /**
     * Return the songlist when call this method
     * @return arraylist that has song inside
     */
    public ArrayList<Song> getSongList() {
        return this.songList;
    }

    /**
     * A check method that check if the some song is inside the album
     *
     * @param songName this is the song name that want to check if exist
     * @return true if it contains else otherwise.
     */
    public boolean contains(String songName) {
        if(this.cacheCheck.contains(songName)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Override the compareTo method to compare two different album by the name
     *
     * @param o object that want to compare
     * @return 1 if they are same 0 not.
     */
    @Override
    public int compareTo(@NonNull Object o) {
        Album other = (Album)o;
        return this.name.compareTo(other.getName());
    }
}
