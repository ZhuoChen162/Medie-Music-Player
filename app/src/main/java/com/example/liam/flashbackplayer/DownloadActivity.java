package com.example.liam.flashbackplayer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class DownloadActivity extends AppCompatActivity {
    public static final String EXTRA_URL = "com.example.liam.flashbackplayer.URL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        Button downloadBtn = (Button) findViewById(R.id.dlBtn);
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText urlField = (EditText) findViewById(R.id.urlField);
                String url = urlField.getText().toString();
                if(!url.equals("")) {
                    Intent output = new Intent();
                    output.putExtra(EXTRA_URL, url);
                    setResult(RESULT_OK, output);
                    finish();
                }
            }
        });
    }
}
