package com.example.myapplication;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
//import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.myapplication.UdpClientThread;

public class MainActivity extends AppCompatActivity {

    EditText editTextAddress, editTextPort;
    Button buttonConnect;
    TextView textViewState, textViewRx;
    ImageButton imageButton;
    ImageButton imageButton2;
    SoundMeter soundMeter;
    TextView tv;
    Button btnRefreshAmpl;

    UdpClientHandler udpClientHandler;
    UdpClientThread udpClientThread;

    private String ip;
    private int port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextAddress = (EditText) findViewById(R.id.address);
        editTextPort = (EditText) findViewById(R.id.port);
        buttonConnect = (Button) findViewById(R.id.connect);
        textViewState = (TextView)findViewById(R.id.state);
        textViewRx = (TextView)findViewById(R.id.received);

        imageButton = findViewById(R.id.imageButton);
        imageButton2 = findViewById(R.id.imageButton2);
        tv = (TextView)findViewById(R.id.textView3);

        buttonConnect.setOnClickListener(buttonConnectOnClickListener);
        udpClientHandler = new UdpClientHandler(this);
        soundMeter = new SoundMeter();

        btnRefreshAmpl = (Button)findViewById(R.id.refresh_amplitude);
        btnRefreshAmpl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tv.setText(Double.toString(soundMeter.getAmplitude()));
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                soundMeter.start();
                tv.setText(Double.toString(soundMeter.getAmplitude()));
                //tv.setText("blabla");
            }
        });

    }

    View.OnClickListener buttonConnectOnClickListener =
            new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    udpClientThread = new UdpClientThread(
                            editTextAddress.getText().toString(),
                            Integer.parseInt(editTextPort.getText().toString()),
                            udpClientHandler, soundMeter);
                    udpClientThread.start();

                    buttonConnect.setEnabled(false);
                }
            };

    private void updateState(String state){
        textViewState.setText(state);
    }

    private void updateRxMsg(String rxmsg){
        textViewRx.append(rxmsg + "\n");
    }

    private void clientEnd(){
        udpClientThread = null;
        textViewState.setText("clientEnd()");
        buttonConnect.setEnabled(true);

    }

    public static class UdpClientHandler extends Handler {
        public static final int UPDATE_STATE = 0;
        public static final int UPDATE_MSG = 1;
        public static final int UPDATE_END = 2;
        private MainActivity parent;

        public UdpClientHandler(MainActivity parent) {
            super();
            this.parent = parent;
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case UPDATE_STATE:
                    parent.updateState((String)msg.obj);
                    break;
                case UPDATE_MSG:
                    parent.updateRxMsg((String)msg.obj);
                    break;
                case UPDATE_END:
                    parent.clientEnd();
                    break;
                default:
                    super.handleMessage(msg);
            }

        }
    }

    public static class SoundMeter {

        private AudioRecord ar = null;
        private int minSize;

        public void start() {
            minSize= AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            ar = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,minSize);
            ar.startRecording();
        }

        public void stop() {
            if (ar != null) {
                ar.stop();
            }
        }

        public double getAmplitude() {
            short[] buffer = new short[minSize];
            ar.read(buffer, 0, minSize);
            int max = 0;
            for (short s : buffer)
            {
                if (Math.abs(s) > max)
                {
                    max = Math.abs(s);
                }
            }
            return max;
        }

        public void getData(short[] buffer) {
            ar.read(buffer, 0, minSize);
        }

    }
}