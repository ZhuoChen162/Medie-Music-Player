package com.example.liam.flashbackplayer;

import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Class MusicLoader is to let phone load the music from the local phone storage and able to play
 * it after
 */
public class MusicLoader {
    private MediaMetadataRetriever mmr;
    private HashMap<String, Album> albumMap;

    /**
     * mucicLoader constuctor that pass two variable and set up the music
     * @param retriever it retriever the music for you
     * @param prefs able to swtich
     */
    public MusicLoader(MediaMetadataRetriever retriever, SharedPreferenceDriver prefs) {
        this.mmr = retriever;

        HashMap<String, Album> stored = prefs.getAlbumMap("album map");

//------------for testing only------------------
        if (stored != null) {
            for (Map.Entry<String, Album> pair : stored.entrySet()) {
                try {
                    Album album = pair.getValue();
                    int[] checkTime = album.getSongList().get(0).getTimePeriod();
                    int[] checkDay = album.getSongList().get(0).getDay();
                    if (checkTime == null || checkDay == null) {
                        prefs.remove("album map");
                        album = null;
                    }
                } catch (Exception e) {
                    break;
                }
                break;
            }
        }
//------------for testing only------------------

        this.albumMap = (stored == null) ? new HashMap<String, Album>() : stored;
    }

    /**
     * initial the file and able to populate the file for you when you call this method
     */
    public void init() {
        File musicDir = readMusicFiles();
        populateAlbumMap(musicDir);
    }

    /**
     * This is the file to read the music file when call it and return the file after
     * @return null
     */
    private File readMusicFiles() {
        //check if storage is mounted (aka read- and write- capable) or at least read-only mounted
        String state = Environment.getExternalStorageState();
        if (!(Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))) {
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

    /**
     * This method is to populate the album map with the given file
     * @param root file that want to populate
     */
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

    /**
     * Load metadata from songs and construct albums
     * @param song file that want to load for albums
     */
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
                if (!toEdit.contains(songName)) {
                    Song newSong = new LocalSong(songName, song.getPath(), artist,
                            trueLength, albumName);
                    toEdit.addSong(newSong);
                    albumMap.put(albumName, toEdit);
                }
            } else {
                Album toAdd = new Album(albumName);
                Song newSong = new LocalSong(songName, song.getPath(), artist, trueLength, albumName);
                toAdd.addSong(newSong);
                albumMap.put(albumName, toAdd);
            }
            fis.close();

        } catch (Exception e) {
            //Log.e("POPULATE ALBUM MAP", song.getPath() + "failed: " + e.getMessage());
        }
    }

    /**
     * Return the hash map of the album map that you build
     * @return album hash map
     */
    public HashMap<String, Album> getAlbumMap() {
        return this.albumMap;
    }
}
