package com.example.liam.flashbackplayer;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Button;

import java.util.ArrayList;

public class MusicController {
    private Activity activity;
    private MediaPlayer mediaPlayer;

    private int skipActive;
    private int currSong;
    private int playMode;
    private AppMediator appMediator;

    public MusicController(MediaPlayer player, Activity activity) {
        this.mediaPlayer = player;
        this.activity = activity;
    }

    public void release() {
        if(!this.isNull()) {
            this.mediaPlayer.release();
        }
    }

    public void playSong(final Song toPlay) {
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
            appMediator.songCompleted(playMode, toPlay);
            }
        });

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(toPlay.getSource());
            mediaPlayer.prepare();
            mediaPlayer.start();
            skipActive = currSong;
        } catch (Exception e) {
            Log.e("LOAD MEDIA", e.getMessage());
        }

        appMediator.startPlay(toPlay);
        appMediator.updateSeekBar(mediaPlayer.getDuration());
    }

    /**
     * skip forward or backward. Direction = -1 for back, 1 for forward.
     *
     * @param direction 1 is forward -1 is backward.
     */
    public void skipSong(int direction) {
        Drawable playImg = activity.getResources().getDrawable(R.drawable.ic_play);
        Button playPause = (Button) activity.findViewById(R.id.buttonPlay);
        ArrayList<Song> songs = new ArrayList<Song>();
        if (playMode == MainActivity.MODE_FLASHBACK) {
            songs = MainActivity.flashbackList;
        } else if (playMode == MainActivity.MODE_ALBUM) {
            songs = MainActivity.perAlbumList;
        } else {
            songs = MainActivity.masterList;
        }
        if (currSong == songs.size() - 1 && direction == 1) {
            try {
                mediaPlayer.stop();
                mediaPlayer.prepare();
                playPause.setBackground(playImg);
                currSong = skipActive;
            } catch (Exception e) {
                Log.e("SKIP SONG", e.getMessage());
            }
        } else if (currSong == 0 && direction == -1) {
            try {
                mediaPlayer.stop();
                mediaPlayer.prepare();
                playPause.setBackground(playImg);
                currSong = skipActive;
            } catch (Exception e) {
                Log.e("SKIP SONG", e.getMessage());
            }
        } else {
            currSong += direction;
            Log.i("PREF", (songs.get(currSong).getPreference() == Song.DISLIKE) ? "DISLIKE" : "OTHER");
            if (songs.get(currSong).getPreference() == Song.DISLIKE) {
                skipSong(direction);
            } else {
                playSong(songs.get(currSong));
            }
        }
    }


    //getters and setters
    public int getPlayMode() {
        return this.playMode;
    }

    public void setPlayMode(int playMode) {
        this.playMode = playMode;
    }

    public int getCurrSong() {
        return currSong;
    }

    public void setCurrSong(int currSong) {
        this.currSong = currSong;
    }

    public void setAppMediator(AppMediator mediator) {
        this.appMediator = mediator;
    }

    public boolean isNull() {
        return this.mediaPlayer == null;
    }

    public boolean isPlaying() {
        return this.mediaPlayer.isPlaying();
    }

    public int getCurrentPosition() {
        return this.mediaPlayer.getCurrentPosition();
    }

    public void setVolume(int i) {
        this.mediaPlayer.setVolume(i / 100f, i / 100f);
    }

    public void seekTo(int i) {
        this.mediaPlayer.seekTo(i);
    }
}
