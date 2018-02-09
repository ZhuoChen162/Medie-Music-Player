package com.example.liam.flashbackplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.View;
import android.media.MediaPlayer;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    private static HashMap<String, Album> albumMap;
    private static MediaMetadataRetriever mmr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermsExplicit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        for(int res : grantResults) {
            if(res != PackageManager.PERMISSION_GRANTED) {
                System.exit(0);
            }
        }
        initAndLoad();
    }

    private void getPermsExplicit() {
        //get explicit permission to read from external storage
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        } else {
            initAndLoad();
        }
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
            File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
            Log.d("readMusicFiles", musicDir.getName());
            String[] children = musicDir.list();
            if(children != null) {
                for(String str : children) {
                    Log.d("readMusicFiles", str);
                }
            }
            return musicDir;
        } catch(Exception e) {
            Log.e("readMusicFiles", e.getMessage());
        }
        return null;
    }

    private void populateAlbumMap(File rootDir) {
        //if a file in the Music directory is not another directory, it must be a song
        if(!rootDir.isDirectory()) {
            populateAlbumWithSong(rootDir);
            return;
        }
        //if it is a directory, recurse until we find songs.
        File[] dirContents = rootDir.listFiles();
        if(dirContents != null) {
            for(File inRoot : dirContents) {
                populateAlbumMap(inRoot);
            }
        }
    }

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
            if(albumName == null) {
                albumName = "Unknown Album";
            }
            if(songName == null) {
                songName = song.getName();
            }
            if(artist == null) {
                artist = "Unknown Artist";
            }
            if(length != null) {
                trueLength = Integer.parseInt(length);
            }

            //update album in map if it already exists, otherwise create the album
            if(albumMap.containsKey(albumName)) {
                Album toEdit = albumMap.get(albumName);
                toEdit.addSong(new Song(songName, song.getPath(), artist, trueLength, albumName));
                albumMap.put(albumName, toEdit);
            } else {
                Album toAdd = new Album(albumName);
                toAdd.addSong(new Song(songName, song.getPath(), artist, trueLength, albumName));
                albumMap.put(albumName, toAdd);
            }
        } catch (Exception e) {
            Log.e("POPULATE ALBUM MAP", song.getPath() + "failed: " + e.getMessage());
        }
    }

    public void initAndLoad() {
        albumMap = new HashMap<String, Album>();
        mmr = new MediaMetadataRetriever();
        File musicDir = readMusicFiles();
        populateAlbumMap(musicDir);
        for(Album toPrint : albumMap.values()) {
            for(Song song : toPrint.getSongList()) {
                String debug = "Album Name: " + toPrint.getName() + ", Song Name: " + song.getName();
                Log.d("MUSIC LOADED", debug);
            }
        }

        //update UI in "song" mode
        ListView listView = (ListView) findViewById(R.id.songDisplay);
        final ArrayList<Song> masterList = new ArrayList<Song>();
        for(Album toAdd : albumMap.values()) {
            masterList.addAll(toAdd.getSongList());
        }
        Object[] listViewArr = masterList.toArray();
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text1, masterList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(masterList.get(position).getName());
                text2.setText(masterList.get(position).getAlbumName());
                return view;
            }
        };
        listView.setAdapter(adapter);
        listView.setSelection(0);

    }
}
