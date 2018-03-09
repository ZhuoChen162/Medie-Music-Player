package com.example.liam.flashbackplayer;


public class History {
    private Song song;
    private String time;
    public History(Song song, String time) {
        this.song = song;
        this.time = time;
    }

    public Song getSong() {
        return this.song;
    }

    public String getTime() {
        return  this.time;
    }
}
