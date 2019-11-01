package com.example.myapplication;

        import java.io.IOException;
        import java.net.DatagramPacket;
        import java.net.DatagramSocket;
        import java.net.InetAddress;
        import java.net.UnknownHostException;

        import android.app.Activity;
        import android.media.AudioFormat;
        import android.media.AudioRecord;
        import android.media.MediaRecorder;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.Button;
        import android.widget.ImageButton;
        import android.widget.EditText;

public class MainActivity extends Activity {
    private Button startButton,stopButton;
    private ImageButton imageStart, imageStop;
    EditText editTextAddress, editTextPort;

    public byte[] buffer;
    public static DatagramSocket socket;
    private int port=12345;

    AudioRecord recorder;

    private int sampleRate = 16000 ; // 44100 for music
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    private boolean status = true;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageStart = findViewById(R.id.imageButton);
        imageStop = findViewById(R.id.imageButton2);
        editTextAddress = (EditText) findViewById(R.id.address);
        editTextPort = (EditText) findViewById(R.id.port);

        imageStart.setOnClickListener (imageStartListener);
        imageStop.setOnClickListener (imageStopListener);

    }

     private final OnClickListener imageStartListener = new View.OnClickListener() {
        public void onClick(View v) {
            status = true;
            startStreaming();
        }
    };

    private final OnClickListener imageStopListener = new View.OnClickListener() {
        public void onClick(View v) {
            status = false;
            recorder.release();
            Log.d("VS","Recorder released");
        }
    };

    public void startStreaming() {
        Thread streamThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket socket = new DatagramSocket();
                    Log.d("VS", "Socket Created");
                    byte[] buffer = new byte[minBufSize];
                    Log.d("VS","Buffer created of size " + minBufSize);
                    DatagramPacket packet;
                    //final InetAddress destination = InetAddress.getByName("192.168.43.83");
                    String adr_t = editTextAddress.getText().toString();
                    final InetAddress destination = InetAddress.getByName(adr_t);
                    port = Integer.parseInt(editTextPort.getText().toString());
                    Log.d("VS", "Address retrieved");
                    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize*10);
                    Log.d("VS", "Recorder initialized");
                    recorder.startRecording();

                    while(status == true) {
                        //reading data from MIC into buffer
                        minBufSize = recorder.read(buffer, 0, buffer.length);
                        //putting buffer in the packet
                        packet = new DatagramPacket (buffer,buffer.length,destination,port);
                        socket.send(packet);
                        System.out.println("MinBufferSize: " +minBufSize);
                    }
                } catch(UnknownHostException e) {
                    Log.e("VS", "UnknownHostException");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("VS", "IOException");
                }
            }
        });
        streamThread.start();
    }
    /*public double getAmplitude() {
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
    }*/
}