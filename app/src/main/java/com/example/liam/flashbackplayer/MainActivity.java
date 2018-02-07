package com.example.liam.flashbackplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.media.MediaPlayer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermsExplicit();
        readMusicFiles();

//        MediaPlayer mediaPlayer;
//        int MEDIA_RES_ID = R.raw.jazz_in_paris;
//
//        public void loadMedia (int resourceID){
//            if (mediaPlayer == null){
//                mediaPlayer = new MediaPlayer();
//            }
//
//            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mediaPlayer) {
//                    mediaPlayer.start();
//                }
//            });
//
//            AssetFileDescriptor assetFileDescriptor = this.getResources().openRawResourceFd(resourceID);
//            try{
//                mediaPlayer.setDataSource(assetFileDescriptor);
//                mediaPlayer.prepareAsync();
//            }catch (Exception e){
//                System.out.println(e.toString());
//            }
//        }


        // play the current song
        /*Button play = (Button) findViewById(R.id.buttonPlay);
        play.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

//                if (play.isPlaying()) {
//                    mediaPlayer.pause();
//                } else {
//                    mediaPlayer.start();
//                }
            }
        });


        // listener for button playing by songs in alphabetic order
        Button playSongs = (Button) findViewById(R.id.buttonSongs);
        playSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        // listener for button playing by albums
        Button playAlbums = (Button) findViewById(R.id.buttonAlbum);
        playAlbums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        // listener for button playing by flashback
        Button playFlashBack = (Button) findViewById(R.id.buttonFlashBack);
        playFlashBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });


*/
    }

    private void getPermsExplicit() {
        //get explicit permission to read from external storage
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
    }

    private void readMusicFiles() {
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
        } catch(Exception e) {
            Log.d("readMusicFiles", e.getMessage());
        }
    }
}
