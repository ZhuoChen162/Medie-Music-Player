package com.example.liam.flashbackplayer;

import android.app.Activity;
import android.app.DownloadManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.DOWNLOAD_SERVICE;

/**
 * Class MusicLoader is to let phone load the music from the local phone storage and able to play
 * it after
 */
public class MusicLoader {
    private MediaMetadataRetriever mmr;
    private HashMap<String, Album> albumMap;
    private Activity activity;

    /**
     * mucicLoader constuctor that pass two variable and set up the music
     * @param retriever it retriever the music for you
     * @param prefs able to swtich
     */
    public MusicLoader(MediaMetadataRetriever retriever, SharedPreferenceDriver prefs, Activity activity) {
        this.mmr = retriever;
        this.activity = activity;
        HashMap<String, Album> stored = prefs.getAlbumMap("album map");

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
        this.albumMap = (stored == null) ? new HashMap<String, Album>() : stored;
    }

    /**
     * initial the file and able to populate the file for you when you call this method
     */
    public void init() {
        File musicDir = getDefaultMusicDirectory();
        File downloadDir = getDefaultDownloadDirectory();
        populateAlbumMap(musicDir);
        populateAlbumMap(downloadDir);
    }

    /**
     * This is the file to read the music file when call it and return the file after
     * @return null
     */
    private File getDefaultMusicDirectory() {
        //check if storage is mounted (aka read- and write- capable) or at least read-only mounted
        String state = Environment.getExternalStorageState();
        if (!(Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))) {
            Log.e("getDefaultMusicDir", "Error: files cannot be read.");
            System.exit(-1);
        }
        //open default Android music directory
        try {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        } catch (Exception e) {
            Log.e("getDefaultMusicDir", e.getMessage());
        }
        return null;
    }

    private File getDefaultDownloadDirectory() {
        //check if storage is mounted (aka read- and write- capable) or at least read-only mounted
        String state = Environment.getExternalStorageState();
        if (!(Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))) {
            Log.e("getDefaultDownDir", "Error: files cannot be read.");
            System.exit(-1);
        }
        //open default Android music directory
        try {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        } catch (Exception e) {
            Log.e("getDefaultDownDir", e.getMessage());
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
            //ensure a file is an audio file
            try {
                String extension = MimeTypeMap.getFileExtensionFromUrl(root.getCanonicalPath());
                extension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                if(!extension.equals("") && extension.startsWith("audio")) {
                    populateAlbumWithSong(root);
                }
            } catch (Exception e) {
                Log.e("Check MIME type", e.getMessage());
            }

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
     * This is the method that used to download the songs by its URL
     * To use this method, first parse the URL to URI and then pass to it
     * and then it will download the song for you
     * @param uri song's uri
     * @return id reference of the songs
     */

    private long DownloadSongs (Uri uri) {

        long downloadReference;

        // Create request for android download manager
        DownloadManager downloadManager = (DownloadManager)activity.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        //Setting title of request
        request.setTitle("Data Download");

        //Setting description of request
        request.setDescription("Android Data download using DownloadManager.");

        //Set the local destination for the downloaded file to a path
        File destinationFile = new File(getDefaultMusicDirectory(), "temp");
        request.setDestinationUri(Uri.fromFile(destinationFile));
        //Enqueue download and save into referenceId
        downloadReference = downloadManager.enqueue(request);

        return downloadReference;
    }

    /**
     * Return the hash map of the album map that you build
     * @return album hash map
     */
    public HashMap<String, Album> getAlbumMap() {
        return this.albumMap;
    }
}
