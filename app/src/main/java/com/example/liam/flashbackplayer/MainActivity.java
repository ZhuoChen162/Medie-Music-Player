package com.example.liam.flashbackplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.media.MediaPlayer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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
        Button play = (Button) findViewById(R.id.buttonPlay);
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



    }
}
