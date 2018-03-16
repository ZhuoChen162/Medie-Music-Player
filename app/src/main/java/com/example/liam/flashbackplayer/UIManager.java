package com.example.liam.flashbackplayer;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class UIManager {
    private Activity activity;
    private boolean isAlbumExpanded;
    private int displayMode;
    private AppMediator appMediator;
    private final Handler seekBarHandler = new Handler();
    protected SeekBar progressSeekBar;
    private SeekBar volumeControl;
    private int duration;
    private boolean isActive;

    public UIManager(Activity activity) {
        this.activity = activity;
        this.isActive = true;
        this.isAlbumExpanded = false;
        Button skipBack = (
                Button) activity.findViewById(R.id.skipBack);
        skipBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appMediator.shouldSkip(-1);
            }
        });
        Button skipForward = (Button) activity.findViewById(R.id.skipForward);
        skipForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appMediator.shouldSkip(1);
            }
        });

        progressBarInit();
        volumeBarInit();
    }

    public void populateUI(final int mode) {

        // MODE_SONG = 0; MODE_ALBUM = 1; MODE_ARTIST = 4;  MODE_FAVORITE = 5;
        displayMode = mode;
        isAlbumExpanded = false;
        ListView listView = (ListView) activity.findViewById(R.id.songDisplay);
        switch (mode) {

            case (MainActivity.MODE_SONG):
                //Sort the songs alphabetically
                Collections.sort(MainActivity.masterList);

                //custom ArrayAdapter to display both the Song name and Album name on the main screen
                ArrayAdapter<Song> adapter = new ArrayAdapter<Song>(activity, R.layout.song_list, android.R.id.text1, MainActivity.masterList) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        final int pos = position;
                        View view = super.getView(position, convertView, parent);
                        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                        TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                        TextView text3 = (TextView) view.findViewById(R.id.text3);
                        final ImageView fave = (ImageView) view.findViewById(R.id.pref);

                        text1.setText(MainActivity.masterList.get(position).getName());
                        text2.setText(MainActivity.masterList.get(position).getArtist());
                        text3.setText(MainActivity.masterList.get(position).getAlbumName());

                        fave.setImageResource(MainActivity.FAVE_ICONS[MainActivity.masterList.get(position).getPreference()]);
                        fave.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Song song = MainActivity.masterList.get(pos);
                                appMediator.setFaveOnclick(song, fave, pos);

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
                        appMediator.setItemOnclick(displayMode, i);
                    }
                });
                break;


            case (MainActivity.MODE_ALBUM):
                final ArrayList<Album> albums = new ArrayList<Album>();
                albums.addAll(MainActivity.albumMap.values());
                //sort the albums in order
                Collections.sort(albums);

                ArrayAdapter<Album> adapter1 = new ArrayAdapter<Album>(activity, android.R.layout.simple_list_item_2, android.R.id.text1, albums) {
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
                listView.setAdapter(adapter1);
                listView.setSelection(0);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Album clicked = albums.get(i);
                        expandAlbum(clicked);
                    }
                });
                break;

            //added favorite mode and artist mode done by Jackie
            case (MainActivity.MODE_ARTIST):

                //Sort the songs by artists name alphabetically
                Collections.sort(MainActivity.masterList, Song.SongArtistCompartor);

                //custom ArrayAdapter to display both the Song name and Album name on the main screen
                ArrayAdapter<Song> adapter2 = new ArrayAdapter<Song>(activity, R.layout.song_list, android.R.id.text1, MainActivity.masterList) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        final int pos = position;
                        View view = super.getView(position, convertView, parent);
                        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                        TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                        TextView text3 = (TextView) view.findViewById(R.id.text3);
                        final ImageView fave = (ImageView) view.findViewById(R.id.pref);

                        text1.setText(MainActivity.masterList.get(position).getName());
                        text2.setText(MainActivity.masterList.get(position).getArtist());
                        text3.setText(MainActivity.masterList.get(position).getAlbumName());
                        fave.setImageResource(MainActivity.FAVE_ICONS[MainActivity.masterList.get(position).getPreference()]);
                        fave.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Song song = MainActivity.masterList.get(pos);
                                appMediator.setFaveOnclick(song, fave, pos);

                            }
                        });
                        return view;
                    }
                };

                listView.setAdapter(adapter2);
                listView.setSelection(0);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        appMediator.setItemOnclick(displayMode, i);
                    }
                });
                break;


            case (MainActivity.MODE_FAVORITE):

                //Sort the songs by artists favorite
                Collections.sort(MainActivity.masterList, Song.SongFavoriteCompartor);

                //custom ArrayAdapter to display both the Song name and Album name on the main screen
                ArrayAdapter<Song> adapter3 = new ArrayAdapter<Song>(activity, R.layout.song_list, android.R.id.text1, MainActivity.masterList) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        final int pos = position;
                        View view = super.getView(position, convertView, parent);
                        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                        TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                        TextView text3 = (TextView) view.findViewById(R.id.text3);
                        final ImageView fave = (ImageView) view.findViewById(R.id.pref);

                        text1.setText(MainActivity.masterList.get(position).getName());
                        text2.setText(MainActivity.masterList.get(position).getArtist());
                        text3.setText(MainActivity.masterList.get(position).getAlbumName());
                        fave.setImageResource(MainActivity.FAVE_ICONS[MainActivity.masterList.get(position).getPreference()]);
                        fave.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Song song = MainActivity.masterList.get(pos);
                                appMediator.setFaveOnclick(song, fave, pos);

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
                        appMediator.setItemOnclick(displayMode, i);
                    }
                });
                break;


            case (MainActivity.MODE_VIBE):
                ArrayAdapter<Song> adapter4 = new ArrayAdapter<Song>(activity, R.layout.song_list, android.R.id.text1, MainActivity.flashbackList) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        final int pos = position;
                        View view = super.getView(position, convertView, parent);
                        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                        TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                        TextView text3 = (TextView) view.findViewById(R.id.text3);
                        final ImageView fave = (ImageView) view.findViewById(R.id.pref);

                        text1.setText(MainActivity.flashbackList.get(position).getName());
                        text2.setText(MainActivity.flashbackList.get(position).getArtist());
                        text3.setText(MainActivity.flashbackList.get(position).getAlbumName());
                        fave.setImageResource(MainActivity.FAVE_ICONS[MainActivity.flashbackList.get(position).getPreference()]);
                        fave.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Song song = MainActivity.flashbackList.get(pos);
                                appMediator.setFaveOnclick(song, fave, pos);
                            }
                        });
                        return view;
                    }
                };
                listView.setAdapter(adapter4);
                listView.setSelection(0);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        //do nothing when clicked; user should not be able to manually choose song in flashback mode
                    }
                });
                if (appMediator.shouldAutoStart(displayMode, "")) {
                    appMediator.autoStart(displayMode);
                }
                break;
        }
    }

    /**
     * This is the method that will expand the album base on the album name when call it
     *
     * @param toExpand album that want to expand
     */
    private void expandAlbum(Album toExpand) {
        boolean play = appMediator.shouldAutoStart(displayMode, toExpand.getName());
        ListView listView = (ListView) activity.findViewById(R.id.songDisplay);
        isAlbumExpanded = true;
        MainActivity.perAlbumList = toExpand.getSongList();
        ArrayAdapter<Song> adapter = new ArrayAdapter<Song>(activity, R.layout.song_list, android.R.id.text1, MainActivity.perAlbumList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final int pos = position;
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                TextView text3 = (TextView) view.findViewById(R.id.text3);
                final ImageView fave = (ImageView) view.findViewById(R.id.pref);

                text1.setText(MainActivity.perAlbumList.get(position).getName());
                text2.setText(MainActivity.perAlbumList.get(position).getArtist());
                text3.setText(MainActivity.perAlbumList.get(position).getAlbumName());
                fave.setImageResource(MainActivity.FAVE_ICONS[MainActivity.perAlbumList.get(position).getPreference()]);
                fave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Song song = MainActivity.perAlbumList.get(pos);
                        appMediator.setFaveOnclick(song, fave, pos);
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
            appMediator.autoStart(displayMode);
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
    public void displayInfo(String name, String album, String loc, String currTime) {

        TextView songName = (TextView) activity.findViewById(R.id.SongName);
        TextView AlbumName = (TextView) activity.findViewById(R.id.AlbumName);
        TextView currentTime = (TextView) activity.findViewById(R.id.currentTime);
        TextView currentLocation = (TextView) activity.findViewById(R.id.currentLocation);

        songName.setText(name);
        AlbumName.setText("Album: " + album);
        currentTime.setText("PlayTime: " + currTime);
        currentLocation.setText("Location: " + loc);
    }

    /**
     * This method is the build the song prograss bar that allow to speed up to some certain points
     */
    private void progressBarInit() {
        progressSeekBar = activity.findViewById(R.id.player_seekbar);
        progressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seek, int i, boolean b) {
                if (b && !appMediator.nullPlayer()) {
                    appMediator.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        final TextView progressTime = activity.findViewById(R.id.cur_progress_time);
        final TextView leftTime = activity.findViewById(R.id.cur_left_time);

        final Runnable seekBarUpdate = new Runnable() {
            public void run() {
                if (isActive && appMediator.canSeek()) {
                    int curProgress = appMediator.getCurrentPosition();
                    progressSeekBar.setProgress(curProgress);
                    progressTime.setText(milliSecToTime(curProgress, true));
                    leftTime.setText(milliSecToTime(duration - curProgress, false));
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
        final SharedPreferenceDriver volumeMem = new SharedPreferenceDriver(activity.getPreferences(Context.MODE_PRIVATE));
        int lastVolume = volumeMem.getVolume();

        volumeControl = activity.findViewById(R.id.player_volume);
        if (lastVolume < 0) {
            volumeControl.setProgress(50);
        } else {
            volumeControl.setProgress(lastVolume);
        }

        volumeControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b && !appMediator.nullPlayer()) {
                    appMediator.setVolume(i);
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

    public void setAppMediator(AppMediator mediator) {
        this.appMediator = mediator;
    }

    public boolean isAlbumExpanded() {
        return this.isAlbumExpanded;
    }

    public void setIsAlbumExpanded(boolean isAlbumExpanded) {
        this.isAlbumExpanded = isAlbumExpanded;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
