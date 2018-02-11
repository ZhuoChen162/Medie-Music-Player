package com.example.liam.flashbackplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.View;
import android.media.MediaPlayer;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    public static final int MODE_SONG = 0;
    public static final int MODE_ALBUM = 1;
    public static final int MODE_FLASHBACK = 2;

    private HashMap<String, Album> albumMap;
    private MediaMetadataRetriever mmr;
    private ArrayList<Song> masterList;
    private ArrayList<Song> perAlbumList;
    private MediaPlayer mediaPlayer;
    private SeekBar progressSeekBar;
    private SeekBar volumeControl;
    private SharedPreferenceDriver prefs;
    private File[] cacheCheck;
    private boolean isAlbumExpanded;
    private boolean isActive;

    private final Handler seekBarHandler = new Handler();

    private int currSong;
    private int playMode;
    private int displayMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermsExplicit();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        Button skipBack = (Button) findViewById(R.id.skipBack);
        skipBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currSong--;
                switch (playMode) {
                    case (MODE_SONG):
                        if(currSong < 0) {
                            currSong = 0;
                            playSong(masterList.get(currSong));
                            mediaPlayer.pause();
                        } else {
                            playSong(masterList.get(currSong));
                        }
                        break;
                    case (MODE_ALBUM):
                        if(currSong < 0) {
                            currSong = 0;
                            playSong(perAlbumList.get(currSong));
                            mediaPlayer.pause();
                        } else {
                            playSong(perAlbumList.get(currSong));
                        }
                        break;
                    case (MODE_FLASHBACK):
                        //get new flashback song
                        break;
                    default:
                        break;
                }
            }
        });
        Button skipForward = (Button) findViewById(R.id.skipForward);
        skipForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currSong++;
                switch (playMode) {
                    case (MODE_SONG):
                        if (currSong >= masterList.size()) {
                            currSong = masterList.size()-1;
                            playSong(masterList.get(currSong));
                            mediaPlayer.pause();
                        } else {
                            playSong(masterList.get(currSong));
                        }
                        break;
                    case (MODE_ALBUM):
                        if(currSong >= perAlbumList.size()) {
                            currSong = perAlbumList.size()-1;
                            playSong(perAlbumList.get(currSong));
                            mediaPlayer.pause();
                        } else {
                            playSong(perAlbumList.get(currSong));
                        }
                        break;
                    case (MODE_FLASHBACK):
                        //get new flashback song
                        break;
                    default:
                        break;
                }
            }
        });


        // play the current song
        Button play = (Button) findViewById(R.id.buttonPlay);
        play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
            }
        });

        Button pause = (Button) findViewById(R.id.buttonPause);
        pause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            }
        });

        progressBarInit();
        volumeBarInit();

        // listener for button playing by songs in alphabetic order
        Button playSongs = (Button) findViewById(R.id.buttonSongs);
        playSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayMode = MODE_SONG;
                populateUI(displayMode);
            }
        });

        // listener for button playing by albums
        Button playAlbums = (Button) findViewById(R.id.buttonAlbum);
        playAlbums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayMode = MODE_ALBUM;
                populateUI(displayMode);
            }
        });

        /*
        // listener for button playing by flashback
        Button playFlashBack = (Button) findViewById(R.id.buttonFlashBack);
        playFlashBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });
*/
    }

    //this method is called when the activity is on its way to destruction. Use it to save data.
    @Override
    protected void onPause() {
        super.onPause();
        prefs.saveObject(albumMap, "albumMap");
        prefs.saveInt(displayMode, "mode");
        isActive = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;
    }

    /*@Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null) {
            mediaPlayer.release();
        }
    }*/

    //use back button to navigate only while in album mode, otherwise default
    @Override
    public void onBackPressed() {
        if(isAlbumExpanded) {
            isAlbumExpanded = false;
            populateUI(displayMode);
        } else {
            super.onBackPressed();
        }
    }

    private void progressBarInit() {
        progressSeekBar = findViewById(R.id.player_seekbar);
        progressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seek, int i, boolean b) {
                if (b && mediaPlayer != null) {
                    mediaPlayer.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        final Runnable seekBarUpdate = new Runnable() {
            public void run() {
                if (isActive && mediaPlayer != null && mediaPlayer.isPlaying()) {
                    progressSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                }
                seekBarHandler.postDelayed(this, 1000);
            }
        };
        seekBarHandler.postDelayed(seekBarUpdate, 1000);
    }

    private void volumeBarInit() {
        volumeControl = findViewById(R.id.player_volume);
        volumeControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b && mediaPlayer != null) {
                    mediaPlayer.setVolume(i / 100f, i / 100f);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        for (int res : grantResults) {
            if (res != PackageManager.PERMISSION_GRANTED) {
                System.exit(0);
            }
        }
        initAndLoad();
    }

    private void getPermsExplicit() {
        //get explicit permission to read and write external storage
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION}, 100);
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
            return musicDir;
        } catch (Exception e) {
            Log.e("readMusicFiles", e.getMessage());
        }
        return null;
    }

    private void populateAlbumMap(File rootDir) {
        //if a file in the Music directory is not another directory, it must be a song
        if (!rootDir.isDirectory()) {
            populateAlbumWithSong(rootDir);
            return;
        }
        //if it is a directory, recurse until we find songs.
        File[] dirContents = rootDir.listFiles();
        if (dirContents != null) {
            for (File inRoot : dirContents) {
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
                toEdit.addSong(new Song(songName, song.getPath(), artist, trueLength, albumName));
                albumMap.put(albumName, toEdit);
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

    private void initAndLoad() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        prefs = new SharedPreferenceDriver(getPreferences(MODE_PRIVATE));
        //cacheCheck = prefs.getFileArr("cache check");
        currSong = 0;
        //defaults to song mode
        displayMode = prefs.getInt("mode");
        isAlbumExpanded = false;
        albumMap = new HashMap<String, Album>();
        mmr = new MediaMetadataRetriever();
        File musicDir = readMusicFiles();
        /*if (cacheCheck != null && Arrays.equals(cacheCheck, musicDir.listFiles())) {
            Log.i("SAVE DIR", "EQUAL DIRECTORIES");
            albumMap = prefs.getAlbumMap("album map");
            if (albumMap == null) {
                populateAlbumMap(musicDir);
                prefs.saveObject(albumMap, "album map");
            }
        } else {
            Log.i("SAVE DIR", "UNEQUAL DIRECTORIES");
            prefs.saveObject(musicDir.listFiles(), "cache check");
            populateAlbumMap(musicDir);
            prefs.saveObject(albumMap, "album map");
            /*for(Album toPrint : albumMap.values()) {
                for(Song song : toPrint.getSongList()) {
                    String debug = "Album Name: " + toPrint.getName() + ", Song Name: " + song.getName();
                    Log.d("MUSIC LOADED", debug);
                }
            }
        }*/
        populateAlbumMap(musicDir);
        populateUI(displayMode);

    }

    private void populateUI(final int mode) {
        isAlbumExpanded = false;
        ListView listView = (ListView) findViewById(R.id.songDisplay);
        switch(mode) {
            case(MODE_SONG):
                masterList = new ArrayList<Song>();
                for (Album toAdd : albumMap.values()) {
                    masterList.addAll(toAdd.getSongList());
                }
                //custom ArrayAdapter to display both the Song name and Album name on the main screen
                ArrayAdapter<Song> adapter = new ArrayAdapter<Song>(this, android.R.layout.simple_list_item_2, android.R.id.text1, masterList) {
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
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        playMode = displayMode;
                        Song clicked = masterList.get(i);
                        currSong = i;
                        playSong(clicked);
                    }
                });
                break;
            case(MODE_ALBUM):
                final ArrayList<Album> albums = new ArrayList<Album>();
                albums.addAll(albumMap.values());
                ArrayAdapter<Album> adapter2 = new ArrayAdapter<Album>(this, android.R.layout.simple_list_item_2, android.R.id.text1, albums) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                        TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                        text1.setText(albums.get(position).getName());
                        text2.setText(albums.get(position).getSongList().size() + " tracks");
                        return view;
                    }
                };
                listView.setAdapter(adapter2);
                listView.setSelection(0);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Album clicked = albums.get(i);
                        expandAlbum(view, clicked);
                    }
                });
        }
    }

    private void expandAlbum(View view, Album toExpand) {
        boolean play = true;
        if(perAlbumList != null && currSong < perAlbumList.size() && playMode == displayMode && perAlbumList.get(currSong).getAlbumName().equals(toExpand.getName())) {
            play = false;
        }
        ListView listView = (ListView) findViewById(R.id.songDisplay);
        isAlbumExpanded = true;
        perAlbumList = toExpand.getSongList();
        ArrayAdapter<Song> adapter = new ArrayAdapter<Song>(this, android.R.layout.simple_list_item_2, android.R.id.text1, perAlbumList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(perAlbumList.get(position).getName());
                text2.setText(perAlbumList.get(position).getAlbumName());
                return view;
            }
        };
        listView.setAdapter(adapter);
        listView.setSelection(0);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //do nothing when clicked; user should not be able to manually choose song in album mode
            }
        });
        if(play) {
            playMode = displayMode;
            playSong(perAlbumList.get(0));
        }
    }

    private void playSong(Song toPlay) {
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(playMode == MODE_ALBUM) {
                    Log.i("SONG DONE", perAlbumList.get(currSong).getName());
                    if(currSong >= perAlbumList.size()-1) {
                        mediaPlayer.reset();
                    } else {
                        currSong++;
                        playSong(perAlbumList.get(currSong));
                    }
                }
            }
        });

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(toPlay.getFileName());
            mediaPlayer.prepare();
            mediaPlayer.start();
            progressSeekBar.setMax(mediaPlayer.getDuration());
        } catch (Exception e) {
            Log.e("LOAD MEDIA", e.getMessage());
        }
    }

}
