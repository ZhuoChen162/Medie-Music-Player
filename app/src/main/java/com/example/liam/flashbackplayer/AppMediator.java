package com.example.liam.flashbackplayer;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.PriorityQueue;

public class AppMediator {
    private FlashbackManager flashbackManager;
    private MusicController musicController;
    private UIManager uiManager;
    private FirebaseService fbs;
    private Activity activity;

    private String userId;

    public AppMediator(FlashbackManager fbm, MusicController mc, UIManager uim, FirebaseService fbs, Activity act) {
        this.flashbackManager = fbm;
        this.musicController = mc;
        this.uiManager = uim;
        this.fbs = fbs;
        this.activity = act;
        this.flashbackManager.setAppMediator(this);
        this.musicController.setAppMediator(this);
        this.uiManager.setAppMediator(this);
    }

    public void setUserId(String id) {
        userId = id;
    }

    public void setFaveOnclick(Song song, ImageView fave, int pos) {
        song.changePreference();
        fave.setImageResource(MainActivity.FAVE_ICONS[song.getPreference()]);
        if (song.getPreference() == Song.DISLIKE && musicController.getCurrSong() == pos) {
            musicController.skipSong(1);
        }
    }

    public void setItemOnclick(int displayMode, int index) {
        switch (displayMode) {
            case (MainActivity.MODE_SONG):
                musicController.setPlayMode(displayMode);
                Song clicked = MainActivity.masterList.get(index);
                musicController.setCurrSong(index);
                musicController.playSong(clicked);
                break;
            case (MainActivity.MODE_ALBUM):

        }
    }

    public boolean shouldAutoStart(int displayMode, String name) {
        if (displayMode == MainActivity.MODE_ALBUM) {
            return !(MainActivity.perAlbumList != null
                    && musicController.getCurrSong() < MainActivity.perAlbumList.size()
                    && musicController.getPlayMode() == displayMode
                    && MainActivity.perAlbumList.get(musicController.getCurrSong()).getAlbumName().equals(name));
        } else if (displayMode == MainActivity.MODE_VIBE) {
            return (musicController.getPlayMode() != displayMode);
        }
        return false;
    }

    public void autoStart(int displayMode) {
        if (displayMode == MainActivity.MODE_ALBUM) {
            musicController.setPlayMode(displayMode);
            musicController.setCurrSong(0);
            if (MainActivity.perAlbumList.get(musicController.getCurrSong()).getPreference() == Song.DISLIKE) {
                musicController.skipSong(1);
            } else {
                musicController.playSong(MainActivity.perAlbumList.get(musicController.getCurrSong()));
            }
        } else if (displayMode == MainActivity.MODE_VIBE) {
            musicController.setCurrSong(0);
            if (MainActivity.flashbackList.size() != 0) {
                musicController.playSong(MainActivity.flashbackList.get(musicController.getCurrSong()));
                musicController.setPlayMode(displayMode);
            } else {
                Toast.makeText(activity.getApplicationContext(), "No song history yet. Play or favorite songs to get started!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void songCompleted(int playMode, Song played) {
        //only when the song is completed,
        //store location, day of week, hour and last played time        ZHAOKAI XU
        SongLocation songLocation = new SongLocation(flashbackManager.getLongitude(), flashbackManager.getLatitude());
        played.updateMetadata(songLocation, flashbackManager.getDayOfWeek(), flashbackManager.getHour(), flashbackManager.getLastPlayedTime());

        fbs.uploadPlayInfo(played.getId(), songLocation, flashbackManager.getYearAndDay(), userId);

        if (playMode == MainActivity.MODE_ALBUM) {
            Log.i("SONG DONE", MainActivity.perAlbumList.get(musicController.getCurrSong()).getName());
            musicController.skipSong(1);
        }
        if (playMode == MainActivity.MODE_VIBE) {
            Log.i("SONG DONE", MainActivity.flashbackList.get(musicController.getCurrSong()).getName());

            //update curr loc and time to implement the ranking algorihtm
            GPSTracker gps = new GPSTracker(flashbackManager.getContext());
            flashbackManager.updateLocAndTime(gps, Calendar.getInstance());
            flashbackManager.rankSongs(MainActivity.masterList);
            PriorityQueue<Song> pq = flashbackManager.getRankList();

            //add songs in pq into the flashbackList
            while (!pq.isEmpty()) {
                if (!MainActivity.flashbackList.contains(pq.peek())) {
                    MainActivity.flashbackList.add(pq.poll());
                    break;
                }
            }
            musicController.skipSong(1);
        }
    }

    public void startPlay(Song playing, GPSTracker gps, Calendar cal) {
        //update curr loc and time, for display and storage
        flashbackManager.updateLocAndTime(gps, cal);
        uiManager.displayInfo(playing.getName(), playing.getAlbumName(), flashbackManager.getAddressKey(), flashbackManager.getCurrTime());
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Drawable pauseImg = activity.getResources().getDrawable(R.drawable.ic_pause);
                Button playPause = (Button) activity.findViewById(R.id.buttonPlay);
                playPause.setBackground(pauseImg);
            }

        });
        MainActivity.addToHistory(playing, Calendar.getInstance());
    }

    public void updateSeekBar(int duration) {
        uiManager.progressSeekBar.setMax(duration);
        uiManager.setDuration(duration);
    }

    public void shouldSkip(int direction) {
        musicController.skipSong(direction);
    }

    public boolean canSeek() {
        return (!musicController.isNull() && musicController.isPlaying());
    }

    public int getCurrentPosition() {
        return musicController.getCurrentPosition();
    }

    public boolean nullPlayer() {
        return musicController.isNull();
    }

    public void setVolume(int i) {
        musicController.setVolume(i);
    }

    public void seekTo(int i) {
        musicController.seekTo(i);
    }

    public void release() {
        musicController.release();
    }

    public int getPlayMode() {
        return this.musicController.getPlayMode();
    }
}
