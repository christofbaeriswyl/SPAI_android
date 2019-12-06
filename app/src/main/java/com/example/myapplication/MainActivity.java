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
        import android.widget.TextView;

public class MainActivity extends Activity {
    private Button startButton,stopButton;
    private ImageButton imageStart, imageStop;
    EditText editTextAddress, editTextPort;
    TextView textAmplitude, textSamplingRate, textBufferSize;

    public byte[] buffer;
    public static DatagramSocket socket;
    private int port=12345;
    private int AmplitudeCounter =0;

    AudioRecord recorder;

    //private int sampleRate = 16000 ; // 44100 for music
    private int sampleRate = 48000 ; // 44100 for music
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    //private int audioFormat = AudioFormat.ENCODING_PCM_FLOAT;
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
        textAmplitude = findViewById(R.id.textAmplitude);
        textSamplingRate = findViewById(R.id.textSamplingRate);
        textBufferSize = findViewById(R.id.textBufferSize);

        imageStart.setOnClickListener (imageStartListener);
        imageStop.setOnClickListener (imageStopListener);

        textBufferSize.setText("Buffer size [byte]: " + Integer.toString(minBufSize*2));
        textSamplingRate.setText("Sampling Rate [Hz]: " + Integer.toString(sampleRate));


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
                    final byte[] buffer = new byte[minBufSize];
                    final byte[] buffer_fp = new byte[minBufSize*2];
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
                        //cast to float
                        int_to_fp(buffer,buffer_fp);
                        //putting buffer in the packet
                        packet = new DatagramPacket (buffer_fp,buffer_fp.length,destination,port);
                        socket.send(packet);

                        AmplitudeCounter++;
                        if (AmplitudeCounter == 10) {
                            AmplitudeCounter = 0;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                  textAmplitude.setText("Amplitude: " + Double.toString(getAmplitude(buffer)));
                                }
                            });
                        }
                        //System.out.println("MinBufferSize: " +minBufSize);
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
    private double getAmplitude(byte[] buffer) {
        int max = 0;
        for (int i = 0; i < buffer.length/2 - 1; i++)
        {
            short val=(short)(((buffer[2*i + 1] & 0xFF) << 8) | (buffer[2*i] & 0xFF));
            if (Math.abs(val) > max)
            {
                max = Math.abs(val);
            }
        }
        return max;
    }
/*
    private double getAmplitude(byte[] buffer) {
        float max = 0;
        for (int i = 0; i < buffer.length/4 - 1; i++)
        {
            float val=(float)(((buffer[4*i + 3] & 0xFF) << 24) | ((buffer[4*i + 2] & 0xFF) << 16) | ((buffer[4*i + 1] & 0xFF) << 8) | (buffer[4*i] & 0xFF));
            if (Math.abs(val) > max)
            {
                max = Math.abs(val);
            }
        }
        return max;
    }
    */
    private void int_to_fp(byte[] buffer, byte[] buffer_fp) {
        float number = 0;
        for (int i = 0; i < buffer.length/2 - 1; i++)
        {
            short val=(short)(((buffer[2*i + 1] & 0xFF) << 8) | (buffer[2*i] & 0xFF));
            float val_fp = val;
            int intBits =  Float.floatToIntBits(val);
            buffer_fp[4*i]     = (byte) (intBits);
            buffer_fp[4*i + 1] = (byte) (intBits >> 8);
            buffer_fp[4*i + 2] = (byte) (intBits >> 16);
            buffer_fp[4*i + 3] = (byte) (intBits >> 24);
  //          int intBits_reconstructed =
   //                 buffer_fp[4*i+3] << 24 | (buffer_fp[4*i+2] & 0xFF) << 16 | (buffer_fp[4*i+1] & 0xFF) << 8 | (buffer_fp[4*i] & 0xFF);
    //        float val_reconstructed = Float.intBitsToFloat(intBits_reconstructed);
        }
    }
}