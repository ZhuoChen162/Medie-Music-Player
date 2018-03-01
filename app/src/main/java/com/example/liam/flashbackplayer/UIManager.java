package com.example.liam.flashbackplayer;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class UIManager {
    private Activity activity;
    private boolean isAlbumExpanded;
    private int displayMode;
    private AppMediator appMediator;

    public UIManager(Activity activity) {
        this.activity = activity;
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
    }

    public void populateUI(final int mode) {
        displayMode = mode;
        isAlbumExpanded = false;
        ListView listView = (ListView) activity.findViewById(R.id.songDisplay);
        switch (mode) {
            case (MainActivity.MODE_SONG):
                Button sortByName = (Button) activity.findViewById(R.id.btn_sortby_name);
                sortByName.getBackground().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
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
                        final ImageView fave = (ImageView) view.findViewById(R.id.pref);

                        text1.setText(MainActivity.masterList.get(position).getName());
                        text2.setText(MainActivity.masterList.get(position).getAlbumName());
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
                Button sortByAlbum = (Button) activity.findViewById(R.id.btn_sortby_album);
                sortByAlbum.getBackground().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
                final ArrayList<Album> albums = new ArrayList<Album>();
                albums.addAll(MainActivity.albumMap.values());
                //sort the albums in order
                Collections.sort(albums);

                ArrayAdapter<Album> adapter2 = new ArrayAdapter<Album>(activity, android.R.layout.simple_list_item_2, android.R.id.text1, albums) {
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
                        expandAlbum(clicked);
                    }
                });
                break;
            case (MainActivity.MODE_FLASHBACK):
                Button flashbackBtn = (Button) activity.findViewById(R.id.buttonFlashBack);
                flashbackBtn.getBackground().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
                ArrayAdapter<Song> adapter3 = new ArrayAdapter<Song>(activity, R.layout.song_list, android.R.id.text1, MainActivity.flashbackList) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        final int pos = position;
                        View view = super.getView(position, convertView, parent);
                        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                        TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                        final ImageView fave = (ImageView) view.findViewById(R.id.pref);

                        text1.setText(MainActivity.flashbackList.get(position).getName());
                        text2.setText(MainActivity.flashbackList.get(position).getAlbumName());
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
                listView.setAdapter(adapter3);
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
                final ImageView fave = (ImageView) view.findViewById(R.id.pref);

                text1.setText(MainActivity.perAlbumList.get(position).getName());
                text2.setText(MainActivity.perAlbumList.get(position).getAlbumName());
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

    public void setAppMediator(AppMediator mediator) {
        this.appMediator = mediator;
    }

    public boolean isAlbumExpanded() {
        return this.isAlbumExpanded;
    }

    public void setIsAlbumExpanded(boolean isAlbumExpanded) {
        this.isAlbumExpanded = isAlbumExpanded;
    }
}
