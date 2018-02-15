package com.example.liam.flashbackplayer;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.util.HashMap;


public class MusicLoader {
    private MediaMetadataRetriever mmr;
    private HashMap<String, Album> albumMap;
    public MusicLoader(MediaMetadataRetriever retriever, SharedPreferenceDriver prefs) {
        this.mmr = retriever;
        HashMap<String, Album> stored = prefs.getAlbumMap("album map");
        this.albumMap = (stored == null) ? new HashMap<String, Album>() : stored;
    }

    public void init() {
        File musicDir = readMusicFiles();
        populateAlbumMap(musicDir);
    }

    private File readMusicFiles() {
        //check if storage is mounted (aka read- and write- capable) or at least read-only mounted
        String state = Environment.getExternalStorageState();
        if (!(Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))) {
            Log.e("readMusicFiles", "Error: files cannot be read.");
            System.exit(-1);
        }
        //open default Android music directory
        try {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        } catch (Exception e) {
            Log.e("readMusicFiles", e.getMessage());
        }
        return null;
    }

    private void populateAlbumMap(File root) {
        //if a file in the Music directory is not another directory, it must be a song
        if (!root.isDirectory()) {
            populateAlbumWithSong(root);
            return;
        }
        //if it is a directory, recurse until we find songs.
        File[] dirContents = root.listFiles();
        if (dirContents != null) {
            for (File newRoot : dirContents) {
                populateAlbumMap(newRoot);
            }
        }
    }

    //load metadata from songs and construct albums
    private void populateAlbumWithSong(File song) {
        try {
            FileInputStream fis = new FileInputStream(song);
            FileDescriptor fd = fis.getFD();
            mmr.setDataSource(fd);
            //check if proper song metadata exists
            String songName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String length = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            String albumName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);

            int trueLength = 0;

            if (albumName == null) {
                albumName = "Unknown Album";
            }
            if (songName == null) {
                songName = song.getName();
            }
            if (artist == null) {
                artist = "Unknown Artist";
            }
            if (length != null) {
                trueLength = Integer.parseInt(length);
            }

            //update album in map if it already exists, otherwise create the album
            if (albumMap.containsKey(albumName)) {
                Album toEdit = albumMap.get(albumName);
                if(!toEdit.contains(songName)) {
                    toEdit.addSong(new Song(songName, song.getPath(), artist, trueLength, albumName));
                    albumMap.put(albumName, toEdit);
                }
            } else {
                Album toAdd = new Album(albumName);
                toAdd.addSong(new Song(songName, song.getPath(), artist, trueLength, albumName));
                albumMap.put(albumName, toAdd);
            }
            fis.close();

        } catch (Exception e) {
            //Log.e("POPULATE ALBUM MAP", song.getPath() + "failed: " + e.getMessage());
        }
    }

    public HashMap<String, Album> getAlbumMap() {
        return this.albumMap;
    }
}
