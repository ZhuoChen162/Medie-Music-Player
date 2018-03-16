package com.example.liam.flashbackplayer;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
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
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.Calendar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Main Activity that build the all the media player functionality such as
 * song mode, album mode, or flashback mode ect.
 */
public class MainActivity extends AppCompatActivity {
    public static final int MODE_SONG = 0;
    public static final int MODE_ALBUM = 1;
    public static final int MODE_VIBE = 2;
    //added modes for display by artist and display by Favorites
    public static final int MODE_ARTIST = 4;
    public static final int MODE_FAVORITE = 5;

    //activity request codes
    private static final int GOOGLE_SIGN_IN = 9002;
    private static final int MOCK_TIME = 9003;
    private static final int DOWNLOAD_MUSIC = 9004;

    public static final int[] FAVE_ICONS = {R.drawable.ic_delete, R.drawable.ic_add, R.drawable.ic_checkmark_sq};

    protected static HashMap<String, Album> albumMap;
    protected static ArrayList<Song> masterList;
    protected static ArrayList<Song> perAlbumList;
    protected static ArrayList<Song> flashbackList;
    protected static ArrayList<History> history;

    protected static HashMap<String, String> emailAndName;
    protected static String myEmail;
    private BroadcastReceiver onComplete;

    protected MediaPlayer mediaPlayer;
    protected SharedPreferenceDriver prefs;
    protected boolean isAlbumExpanded;

    protected int currSong;
    protected int prevMode;
    protected int displayMode;

    //for update loc and time
    protected FlashbackManager flashbackManager = new FlashbackManager(this);
    protected UIManager uiManager;
    protected MusicController musicController;
    protected AppMediator appMediator;
    protected MusicLoader loader;

    private UrlList urlList;
    private FirebaseService fbs;

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
        onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                File downloadedFile = loader.getLastDownloadedFile();
                //do something with the last downloaded file here...
            }
        };
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        getPermsExplicit();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

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

        final Button playerMode = (Button) findViewById(R.id.btnPlayer);
        final Button flashbackMode = (Button) findViewById(R.id.btnFlashback);
        // listener for button playing by songs in alphabetic order
        playerMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flashbackMode.getBackground().clearColorFilter();
                playerMode.getBackground().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
                displayMode = prevMode;
                uiManager.populateUI(displayMode);
            }
        });

        // listener for button playing by flashback         ZHAOKAI XU
        flashbackMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerMode.getBackground().clearColorFilter();
                flashbackMode.getBackground().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
                prevMode = displayMode;
                if (appMediator.getPlayMode() != MODE_VIBE) {
                    flashbackList.clear();
                    GPSTracker gps = new GPSTracker(v.getContext());

                    //update curr loc and time to implement the ranking algorihtm
                    flashbackManager.updateLocAndTime(gps, Calendar.getInstance());
                    flashbackManager.rankSongs(masterList);
                    PriorityQueue<Song> pq = flashbackManager.getRankList();

                    //add songs in pq into the flashbackList
                    while (!pq.isEmpty()) {
                        if (!flashbackList.contains(pq.peek())) {
                            flashbackList.add(pq.poll());
                            break;
                        }
                    }

                    //update UI
                    displayMode = MODE_VIBE;
                    uiManager.populateUI(displayMode);
                } else {
                    displayMode = MODE_VIBE;
                    uiManager.populateUI(displayMode);
                }
            }
        });

        Button signInBtn = (Button) findViewById(R.id.btnSignIn);
        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GoogleLoginActivity.class);
                startActivityForResult(intent, GOOGLE_SIGN_IN);
            }
        });

        Button downloadBtn = (Button) findViewById(R.id.btnDownload);
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DownloadActivity.class);
                startActivityForResult(intent, DOWNLOAD_MUSIC);
            }
        });
    }

    protected void onEnterVibeMode() {
        fbs.makeCloudChangelist(urlList.getUrlMap());

        fbs.updateCloudSongList(urlList.getUrlMap());
    }

    /**
     * this method is called when the activity is on its way to destruction. Use it to save data.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (this.prefs != null) {
            if (albumMap != null) {
                prefs.saveObjectWithSongs(albumMap, "album map");
            }
            if (history != null) {
                prefs.saveObjectWithSongs(history, "history");
            }
            prefs.saveInt(displayMode, "mode");
        }
        if (uiManager != null) {
            uiManager.setIsActive(false);
        }
    }

    /**
     * Method that when call it, the resume function will work.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (uiManager != null) {
            uiManager.setIsActive(true);
        }
    }

    /**
     * Destroy and clear the app data when call
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        appMediator.release();
        unregisterReceiver(onComplete);
    }

    /**
     * use back button to navigate only while in album mode, otherwise default
     */
    @Override
    public void onBackPressed() {
        if (uiManager.isAlbumExpanded()) {
            uiManager.setIsAlbumExpanded(false);
            uiManager.populateUI(displayMode);
        } else {
            super.onBackPressed();
        }
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
        loader = new MusicLoader(new MediaMetadataRetriever(), prefs, this);
        loader.init();
        albumMap = loader.getAlbumMap();
        masterList = new ArrayList<Song>();
        for (Album toAdd : albumMap.values()) {
            masterList.addAll(toAdd.getSongList());
        }
        MusicController musicController = new MusicController(mediaPlayer, this);
        uiManager = new UIManager(this);

        urlList = new UrlList(masterList);
        fbs = new FirebaseService(urlList);

        appMediator = new AppMediator(flashbackManager, musicController, uiManager, fbs, this);

        history = prefs.getHistory("history");
        if (history == null) {
            history = new ArrayList<History>();
        }

        Button playerMode = (Button) findViewById(R.id.btnPlayer);
        Button flashbackMode = (Button) findViewById(R.id.btnFlashback);
        if (displayMode == MODE_VIBE) {
            playerMode.getBackground().clearColorFilter();
            flashbackMode.getBackground().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
            GPSTracker gps = new GPSTracker(this);
            flashbackManager.updateLocAndTime(gps, Calendar.getInstance());
            flashbackManager.rankSongs(masterList);
            PriorityQueue<Song> pq = flashbackManager.getRankList();
            if (!pq.isEmpty()) {
                flashbackList.add(pq.poll());
            }
        } else {
            flashbackMode.getBackground().clearColorFilter();
            playerMode.getBackground().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
        }
        uiManager.populateUI(displayMode);

        onEnterVibeMode();
    }

    private void viewHistory() {
        ArrayAdapter<History> songArrayAdapter = new ArrayAdapter<History>(this, android.R.layout.simple_list_item_2, android.R.id.text1, history) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                if (history.get(position).getSong() != null) {
                    text1.setText(history.get(position).getSong().getName());
                }
                text2.setText(history.get(position).getTime());
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

    protected static void addToHistory(Song currSong, Calendar calendar) {
        if (history.size() > 49) {
            history.remove(49);
        }
        History toAdd = new History(currSong, DateFormat.format("yyyy-MM-dd hh:mm:ss", calendar).toString());
        history.add(0, toAdd);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        // Handle item selection
        switch (itemId) {
            case R.id.btn_settings:
                Intent intent = new Intent(MainActivity.this, MockTimeActivity.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean(MockTimeActivity.EXTRA_UPDATE, flashbackManager.shouldUpdate());
                bundle.putLong(MockTimeActivity.EXTRA_MILLIS, flashbackManager.getMockMillis());
                intent.putExtras(bundle);
                startActivityForResult(intent, MOCK_TIME);
                return true;

            case R.id.btn_history:
                prevMode = displayMode;
                viewHistory();
                ((Button) findViewById(R.id.btnPlayer)).getBackground().clearColorFilter();
                ((Button) findViewById(R.id.btnFlashback)).getBackground().clearColorFilter();
                return true;

            //display sorted by title
            case R.id.btn_sortby_title:
                item.setChecked(true);
                displayMode = MODE_SONG;
                uiManager.populateUI(displayMode);
                return true;

            //display sorted by album
            case R.id.btn_sortby_album:
                item.setChecked(true);
                displayMode = MODE_ALBUM;
                uiManager.populateUI(displayMode);
                return true;

            //display sorted by artiest
            case R.id.btn_sortby_artist:
                item.setChecked(true);
                displayMode = MODE_ARTIST;
                uiManager.populateUI(displayMode);
                return true;

            //display sorted by favorite
            case R.id.btn_sortby_fav:
                item.setChecked(true);
                displayMode = MODE_FAVORITE;
                uiManager.populateUI(displayMode);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case GOOGLE_SIGN_IN:
                if (resultCode == RESULT_OK && data != null) {
                    myEmail = data.getStringExtra(GoogleLoginActivity.EXTRA_MYEMAIL);
                    appMediator.setUserId(myEmail);
                    emailAndName = (HashMap<String, String>) data.getSerializableExtra(GoogleLoginActivity.EXTRA_EMAILLIST);

                    fbs.makePlayList(masterList, emailAndName, 2018072, -122.08400000000002, 37.421998333333335);
                }
                break;

            case MOCK_TIME:
                if (resultCode == RESULT_OK && data != null) {
                    boolean shouldUpdate = data.getBooleanExtra(MockTimeActivity.EXTRA_UPDATE, true);
                    long millis = data.getLongExtra(MockTimeActivity.EXTRA_MILLIS, 0);
                    flashbackManager.setShouldUpdate(shouldUpdate);
                    flashbackManager.setMockMillis(millis);
                }
                break;

            case DOWNLOAD_MUSIC:
                if (resultCode == RESULT_OK && data != null) {
                    String url = data.getStringExtra(DownloadActivity.EXTRA_URL);
                    loader.downloadFromUrl(Uri.parse(url));
                }
                break;
        }
    }

}