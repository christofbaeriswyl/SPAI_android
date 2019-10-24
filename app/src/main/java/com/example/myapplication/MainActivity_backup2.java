/*
package com.example.myapplication;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import android.os.Handler;
import android.os.Message;
//import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

public class MainActivity_backup2 extends AppCompatActivity {

    private String ip;
    private int port;

    EditText editTextAddress, editTextPort;
    Button buttonConnect;
    TextView textViewState, textViewRx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_sound);

        ImageButton imageButton = findViewById(R.id.imageButton);
        ImageButton imageButton2 = findViewById(R.id.imageButton2);

        udpClientHandler = new UdpClientHandler(this);
        //udpConnect = new Thread(new ClientSend_backup()).start();

        final TextView tv = (TextView)findViewById(R.id.textView3);
        final SoundMeter SoundMeter1 = new SoundMeter();
        Button btnAdd = (Button)findViewById(R.id.button);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tv.setText(Double.toString(SoundMeter1.getAmplitude()));
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SoundMeter1.start();
                tv.setText(Double.toString(SoundMeter1.getAmplitude()));
                //tv.setText("blabla");
            }
        });
    }


    }
}
*/