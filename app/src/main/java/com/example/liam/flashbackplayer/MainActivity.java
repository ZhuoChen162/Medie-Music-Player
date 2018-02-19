package com.example.liam.flashbackplayer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.View;
import android.media.MediaPlayer;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Main Activity that build the all the media player functionality such as
 * song mode, album mode, or flashback mode ect.
 */
public class MainActivity extends AppCompatActivity {
    public static final int MODE_SONG = 0;
    public static final int MODE_ALBUM = 1;
    public static final int MODE_FLASHBACK = 2;
    public static final int MODE_HISTORY = 3;

    public static final int[] FAVE_ICONS = {R.drawable.ic_add, R.drawable.ic_delete, R.drawable.ic_checkmark_sq};

    protected HashMap<String, Album> albumMap;
    protected ArrayList<Song> masterList;
    protected ArrayList<Song> perAlbumList;
    protected ArrayList<Song> flashbackList;
    protected ArrayList<Song> history;
    protected ArrayList<String> historyTime;

    protected MediaPlayer mediaPlayer;
    protected SeekBar progressSeekBar;
    protected SeekBar volumeControl;
    protected SharedPreferenceDriver prefs;
    protected boolean isAlbumExpanded;
    protected boolean isActive;
    protected int curMusicDuration;

    protected final Handler seekBarHandler = new Handler();

    private int skipActive;
    protected int currSong;
    protected int playMode;
    protected int displayMode;
    private int prevMode;

    //for update loc and time
    private int date, dayOfWeek, hour, mins;
    private long lastPlayedTime;
    protected String addressKey;
    protected String currTime;
    private double longitude, latitude;

    /**
     * Override the oncreate method to handle the basic button function such as
     * play/pause button, skip forward/back button
     *
     * @param savedInstanceState save the state for the buttons
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermsExplicit();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        Button skipBack = (
                Button) findViewById(R.id.skipBack);
        skipBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                skipSong(-1);
            }
        });
        Button skipForward = (Button) findViewById(R.id.skipForward);
        skipForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                skipSong(1);
            }
        });


        // play the current song
        final Drawable playImg = getResources().getDrawable(R.drawable.ic_play);
        final Drawable pauseImg = getResources().getDrawable(R.drawable.ic_pause);
        final Button playOrPause = findViewById(R.id.buttonPlay);
        playOrPause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    playOrPause.setBackground(playImg);
                    mediaPlayer.pause();
                } else {
                    playOrPause.setBackground(pauseImg);
                    mediaPlayer.start();
                }
            }
        });

        progressBarInit();
        volumeBarInit();

        final Button sortByName = (Button) findViewById(R.id.btn_sortby_name);
        final Button sortByAlbum = (Button) findViewById(R.id.btn_sortby_album);
        final Button playFlashBack = (Button) findViewById(R.id.buttonFlashBack);
        final Button viewHistory = (Button) findViewById(R.id.btn_view_history);
        // listener for button playing by songs in alphabetic order
        sortByName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortByAlbum.getBackground().clearColorFilter();
                playFlashBack.getBackground().clearColorFilter();
                viewHistory.getBackground().clearColorFilter();
                displayMode = MODE_SONG;
                populateUI(displayMode);
            }
        });

        sortByAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortByName.getBackground().clearColorFilter();
                playFlashBack.getBackground().clearColorFilter();
                viewHistory.getBackground().clearColorFilter();
                displayMode = MODE_ALBUM;
                populateUI(displayMode);
            }
        });

        viewHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortByName.getBackground().clearColorFilter();
                sortByAlbum.getBackground().clearColorFilter();
                playFlashBack.getBackground().clearColorFilter();
                viewHistory();
            }
        });

        // listener for button playing by flashback         ZHAOKAI XU
        playFlashBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortByName.getBackground().clearColorFilter();
                sortByAlbum.getBackground().clearColorFilter();
                viewHistory.getBackground().clearColorFilter();
                prevMode = displayMode;
                if (playMode != MODE_FLASHBACK) {
                    flashbackList.clear();
                    GPSTracker gps = new GPSTracker(v.getContext());

                    //update curr loc and time to implement the ranking algorihtm
                    updateLocAndTime(gps, Calendar.getInstance());
                    FlashbackManager fbm = new FlashbackManager(latitude, longitude, dayOfWeek, hour);
                    fbm.rankSongs(masterList);
                    PriorityQueue<Song> pq = fbm.getRankList();

                    //add songs in pq into the flashbackList
                    while(!pq.isEmpty()) {
                        if(!flashbackList.contains(pq.peek())) {
                            flashbackList.add(pq.poll());
                            break;
                        }
                    }

                    //update UI
                    displayMode = MODE_FLASHBACK;
                    populateUI(displayMode);
                } else {
                    displayMode = MODE_FLASHBACK;
                    populateUI(displayMode);
                }
            }
        });

    }

    /**
     * this method is called when the activity is on its way to destruction. Use it to save data.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (this.prefs != null) {
            if (this.albumMap != null) {
                prefs.saveObject(albumMap, "album map");
            }
            if (this.history != null) {
                prefs.saveObject(history, "history");
            }
            if (this.historyTime != null) {
                prefs.saveObject(historyTime, "historyTime");
            }
            prefs.saveInt(displayMode, "mode");
        }
        isActive = false;
    }

    /**
     * Method that when call it, the resume function will work.
     */
    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;
    }

    /**
     * Destroy and clear the app data when call
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    /**
     * use back button to navigate only while in album mode, otherwise default
     */
    @Override
    public void onBackPressed() {
        if (isAlbumExpanded) {
            isAlbumExpanded = false;
            populateUI(displayMode);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * This method is the build the song prograss bar that allow to speed up to some certain points
     */
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

        final TextView progressTime = findViewById(R.id.cur_progress_time);
        final TextView leftTime = findViewById(R.id.cur_left_time);

        final Runnable seekBarUpdate = new Runnable() {
            public void run() {
                if (isActive && mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int curProgress = mediaPlayer.getCurrentPosition();
                    progressSeekBar.setProgress(curProgress);
                    progressTime.setText(milliSecToTime(curProgress, true));
                    leftTime.setText(milliSecToTime(curMusicDuration - curProgress, false));
                }
                seekBarHandler.postDelayed(this, 1000);
            }
        };
        seekBarHandler.postDelayed(seekBarUpdate, 1000);
    }

    /**
     * function that translate the time from millsec to time string
     *
     * @param milliSec time that want to translate
     * @param positive true if want to translate
     * @return the string of the time
     */
    private String milliSecToTime(int milliSec, boolean positive) {
        String time = "";
        String strSeconds = "";

        int minutes = (milliSec % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = ((milliSec % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if (seconds < 10) {
            strSeconds = "0" + seconds;
        } else {
            strSeconds = "" + seconds;
        }

        time = minutes + ":" + strSeconds;
        if (!positive) {
            time = "-" + time;
        }
        return time;
    }

    /**
     * This is the method that build the volume bar and allow to change the volume inside the
     * songs
     */
    private void volumeBarInit() {
        final SharedPreferenceDriver volumeMem = new SharedPreferenceDriver(getPreferences(MODE_PRIVATE));
        int lastVolume = volumeMem.getVolume();

        volumeControl = findViewById(R.id.player_volume);
        if (lastVolume < 0) {
            volumeControl.setProgress(50);
        } else {
            volumeControl.setProgress(lastVolume);
        }

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
                volumeMem.saveVolume(seekBar.getProgress());
            }
        });
    }

    /**
     * Give the permission check for the pass in things
     *
     * @param requestCode  code that want to request
     * @param permissions  that want to use
     * @param grantResults give the result
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        for (int i = 0; i < grantResults.length; i++) {
            int res = grantResults[i];
            if (res != PackageManager.PERMISSION_GRANTED) {
                System.exit(0);
            } else {
                Log.i("PERM GRANTED:", permissions[i]);
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

    private void initAndLoad() {
        flashbackList = new ArrayList<>();
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        prefs = new SharedPreferenceDriver(getPreferences(MODE_PRIVATE));
        currSong = 0;
        //defaults to song mode
        displayMode = prefs.getInt("mode");
        isAlbumExpanded = false;
        MusicLoader loader = new MusicLoader(new MediaMetadataRetriever(), prefs);
        loader.init();
        albumMap = loader.getAlbumMap();
        masterList = new ArrayList<Song>();
        for (Album toAdd : albumMap.values()) {
            masterList.addAll(toAdd.getSongList());
        }

        history = prefs.getHistory("history");
        historyTime = prefs.getHistoryTime("historyTime");
        if (history == null) {
            history = new ArrayList<>(50);
            historyTime = new ArrayList<>(50);
        }

        if(displayMode == MODE_FLASHBACK) {
            GPSTracker gps = new GPSTracker(this);
            updateLocAndTime(gps, Calendar.getInstance());
            FlashbackManager fbm = new FlashbackManager(latitude, longitude, dayOfWeek, hour);
            fbm.rankSongs(masterList);
            PriorityQueue<Song> pq = fbm.getRankList();
            if (!pq.isEmpty()) {
                flashbackList.add(pq.poll());
            }
        }
        populateUI(displayMode);

    }

    /**
     * This will populate the listview that will hold all the songs in the local file and
     * sort them and can scroll up and down to check songs.
     *
     * @param mode current mode that in
     */
    private void populateUI(final int mode) {
        isAlbumExpanded = false;
        ListView listView = (ListView) findViewById(R.id.songDisplay);
        switch (mode) {
            case (MODE_SONG):
                Button sortByName = (Button) findViewById(R.id.btn_sortby_name);
                sortByName.getBackground().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
                //Sort the songs alphabetically
                Collections.sort(masterList);

                //custom ArrayAdapter to display both the Song name and Album name on the main screen
                ArrayAdapter<Song> adapter = new ArrayAdapter<Song>(this, R.layout.song_list, android.R.id.text1, masterList) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        final int pos = position;
                        View view = super.getView(position, convertView, parent);
                        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                        TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                        final ImageView fave = (ImageView) view.findViewById(R.id.pref);

                        text1.setText(masterList.get(position).getName());
                        text2.setText(masterList.get(position).getAlbumName());
                        fave.setImageResource(FAVE_ICONS[masterList.get(position).getPreference()]);
                        fave.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Song song = masterList.get(pos);
                                song.changePreference();
                                fave.setImageResource(FAVE_ICONS[song.getPreference()]);
                                if (song.getPreference() == Song.DISLIKE && currSong == pos) {
                                    skipSong(1);
                                }
                            }
                        });
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
            case (MODE_ALBUM):
                Button sortByAlbum = (Button) findViewById(R.id.btn_sortby_album);
                sortByAlbum.getBackground().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
                playMode = displayMode;
                final ArrayList<Album> albums = new ArrayList<Album>();
                albums.addAll(albumMap.values());
                //sort the albums in order
                Collections.sort(albums);

                ArrayAdapter<Album> adapter2 = new ArrayAdapter<Album>(this, R.layout.song_list, android.R.id.text1, albums) {
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
                break;
            case (MODE_FLASHBACK):
                Button flashbackBtn = (Button) findViewById(R.id.buttonFlashBack);
                flashbackBtn.getBackground().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
                ArrayAdapter<Song> adapter3 = new ArrayAdapter<Song>(this, R.layout.song_list, android.R.id.text1, flashbackList) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        final int pos = position;
                        View view = super.getView(position, convertView, parent);
                        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                        TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                        final ImageView fave = (ImageView) view.findViewById(R.id.pref);

                        text1.setText(flashbackList.get(position).getName());
                        text2.setText(flashbackList.get(position).getAlbumName());
                        fave.setImageResource(FAVE_ICONS[flashbackList.get(position).getPreference()]);
                        fave.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Song song = flashbackList.get(pos);
                                song.changePreference();
                                fave.setImageResource(FAVE_ICONS[song.getPreference()]);
                                if (song.getPreference() == Song.DISLIKE && currSong == pos) {
                                    skipSong(1);
                                }
                            }
                        });
                        return view;
                    }
                };
                listView.setAdapter(adapter3);
                listView.setSelection(0);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        //do nothing when clicked; user should not be able to manually choose song in flashback mode
                    }
                });
                if (playMode != displayMode) {
                    currSong = 0;
                    if (flashbackList.size() != 0) {
                        playSong(flashbackList.get(currSong));
                        playMode = displayMode;
                    } else {
                        Toast.makeText(getApplicationContext(), "No song history yet. Play or favorite songs to get started!", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    /**
     * This is the method that will expand the album base on the album name when call it
     *
     * @param view     view the album on the phone
     * @param toExpand album that want to expand
     */
    private void expandAlbum(View view, Album toExpand) {
        boolean play = true;
        if (perAlbumList != null && currSong < perAlbumList.size() && playMode == displayMode && perAlbumList.get(currSong).getAlbumName().equals(toExpand.getName())) {
            play = false;
        }
        ListView listView = (ListView) findViewById(R.id.songDisplay);
        isAlbumExpanded = true;
        perAlbumList = toExpand.getSongList();
        ArrayAdapter<Song> adapter = new ArrayAdapter<Song>(this, R.layout.song_list, android.R.id.text1, perAlbumList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final int pos = position;
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                final ImageView fave = (ImageView) view.findViewById(R.id.pref);

                text1.setText(perAlbumList.get(position).getName());
                text2.setText(perAlbumList.get(position).getAlbumName());
                fave.setImageResource(FAVE_ICONS[perAlbumList.get(position).getPreference()]);
                fave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Song song = perAlbumList.get(pos);
                        song.changePreference();
                        fave.setImageResource(FAVE_ICONS[song.getPreference()]);
                        if (song.getPreference() == Song.DISLIKE && currSong == pos) {
                            skipSong(1);
                        }
                    }
                });
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

        if (play) {
            playMode = displayMode;
            currSong = 0;
            if (perAlbumList.get(currSong).getPreference() == Song.DISLIKE) {
                skipSong(1);
            } else {
                playSong(perAlbumList.get(currSong));
            }
        }
    }


    /**
     * This is the method when call it to play music when call
     *
     * @param toPlay song to play
     */
    protected void playSong(final Song toPlay) {

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

                //only when the song is completed,
                //store location, day of week, hour and last played time        ZHAOKAI XU
                SongLocation songLocation = new SongLocation(longitude, latitude);
                toPlay.updateMetadata(songLocation, dayOfWeek, hour, lastPlayedTime);

                if (playMode == MODE_ALBUM) {
                    Log.i("SONG DONE", perAlbumList.get(currSong).getName());
                    skipSong(1);
                }
                if (playMode == MODE_FLASHBACK) {
                    Log.i("SONG DONE", flashbackList.get(currSong).getName());
                    GPSTracker gps = new GPSTracker(MainActivity.this);

                    //update curr loc and time to implement the ranking algorihtm
                    updateLocAndTime(gps, Calendar.getInstance());
                    FlashbackManager fbm = new FlashbackManager(latitude, longitude, dayOfWeek, hour);
                    fbm.rankSongs(masterList);
                    PriorityQueue<Song> pq = fbm.getRankList();

                    //add songs in pq into the flashbackList
                    while(!pq.isEmpty()) {
                        if(!flashbackList.contains(pq.peek())) {
                            flashbackList.add(pq.poll());
                            break;
                        }
                    }
                    skipSong(1);
                }

            }
        });

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(toPlay.getFileName());
            mediaPlayer.prepare();
            mediaPlayer.start();
            curMusicDuration = mediaPlayer.getDuration();
            progressSeekBar.setMax(curMusicDuration);
            skipActive = currSong;

            //update curr loc and time, for display and storage
            GPSTracker gps = new GPSTracker(this);
            updateLocAndTime(gps, Calendar.getInstance());

            //display info
            displayInfo(toPlay.getName(), toPlay.getAlbumName(), addressKey, currTime);
            Drawable pauseImg = getResources().getDrawable(R.drawable.ic_pause);
            Button playPause = (Button) findViewById(R.id.buttonPlay);
            playPause.setBackground(pauseImg);

        } catch (Exception e) {
            Log.e("LOAD MEDIA", e.getMessage());
        }

        addToHistory(toPlay, Calendar.getInstance());
    }

    /**
     * skip forward or backward. Direction = -1 for back, 1 for forward.
     *
     * @param direction 1 is forward -1 is backward.
     */
    private void skipSong(int direction) {
        ArrayList<Song> songs = new ArrayList<Song>();
        Drawable playImg = getResources().getDrawable(R.drawable.ic_play);
        Button playPause = (Button) findViewById(R.id.buttonPlay);
        if (playMode == MODE_FLASHBACK) {
            songs = flashbackList;
        } else if (playMode == MODE_ALBUM) {
            songs = perAlbumList;
        } else {
            songs = masterList;
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

    /**
     * function to display info of the song when a song starts playing
     *
     * @param name     of the song
     * @param album    of the song
     * @param loc      when play it
     * @param currTime time when play the song
     */
    protected void displayInfo(String name, String album, String loc, String currTime) {

        TextView songName = (TextView) findViewById(R.id.SongName);
        TextView AlbumName = (TextView) findViewById(R.id.AlbumName);
        TextView currentTime = (TextView) findViewById(R.id.currentTime);
        TextView currentLocation = (TextView) findViewById(R.id.currentLocation);

        songName.setText(name);
        AlbumName.setText("Album: " + album);
        currentTime.setText("PlayTime: " + currTime);
        currentLocation.setText("Location: " + loc);
    }

    /**
     * Update the location and time when call with GPS and time
     *
     * @param gpsTracker location tracker
     * @param calendar   time
     */
    protected void updateLocAndTime(GPSTracker gpsTracker, Calendar calendar) {
        // want to get current locaiton while starting playing the song
        // Created by ZHAOKAI XU:
        longitude = gpsTracker.getLongitude();
        latitude = gpsTracker.getLatitude();

        //convert to addres using geocoder provided by google API
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            Address address = addresses.get(0);
            //store the addressKey
            addressKey = address.getLocality() + address.getFeatureName();
        } catch (Exception e) {
            Log.e("GEOCODER", e.getMessage());
        }

        //get time info to store
        dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        date = calendar.get(Calendar.DATE);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        mins = calendar.get(Calendar.MINUTE);

        //calculate lastPlayedTime in double format
        lastPlayedTime = date * 10000 + hour * 100 + mins;

        //get current time to display
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        currTime = sdf.format(new Date());
    }

    private void viewHistory() {
        Button historyBtn = (Button) findViewById(R.id.btn_view_history);
        historyBtn.getBackground().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
        ArrayAdapter<Song> songArrayAdapter = new ArrayAdapter<Song>(this, android.R.layout.simple_list_item_2, android.R.id.text1, history) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(history.get(position).getName());
                text2.setText(historyTime.get(position));
                return view;
            }
        };
        ListView listView = (ListView) findViewById(R.id.songDisplay);
        listView.setAdapter(songArrayAdapter);
        listView.setSelection(0);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //do nothing when clicked; user should not be able to manually choose song in flashback mode
            }
        });
    }

    private void addToHistory(Song curSong, Calendar calendar) {
        if (history.size() > 49) {
            history.remove(49);
            historyTime.remove(49);
        }
        history.add(0, curSong);
        historyTime.add(0, DateFormat.format("yyyy-MM-dd hh:mm:ss", calendar).toString());
    }
}