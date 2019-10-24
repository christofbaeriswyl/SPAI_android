package com.example.myapplication;

import android.os.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class UdpClientThread extends Thread{

    String dstAddress;
    int dstPort;
    private boolean running;
    MainActivity.UdpClientHandler handler;
    MainActivity.SoundMeter soundmeter;

    DatagramSocket socket;
    InetAddress address;

    public UdpClientThread(String addr, int port, MainActivity.UdpClientHandler handler, MainActivity.SoundMeter soundmeter) {
        super();
        dstAddress = addr;
        dstPort = port;
        this.handler = handler;
        this.soundmeter = soundmeter;
    }

    public void setRunning(boolean running){
        this.running = running;
    }

    private void sendState(String state){
        handler.sendMessage(
                Message.obtain(handler,
                        MainActivity.UdpClientHandler.UPDATE_STATE, state));
    }

    @Override
    public void run() {
        sendState("connecting...");

        running = true;

        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName(dstAddress);

            // send request
            while(true) {
                short[] buffer_short = new short[256];
                soundmeter.getData(buffer_short);
                byte[] buffer = new byte[256 * 2];

                int short_index, byte_index;
                int iterations = 256;
                short_index = byte_index = 0;

                for (/*NOP*/; short_index != iterations; /*NOP*/) {
                    buffer[byte_index] = (byte) (buffer_short[short_index] & 0x00FF);
                    buffer[byte_index + 1] = (byte) ((buffer_short[short_index] & 0xFF00) >> 8);

                    ++short_index;
                    byte_index += 2;
                }

                DatagramPacket packet =
                        new DatagramPacket(buffer, buffer.length, address, dstPort);
                socket.send(packet);

                sendState("connected");
            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(socket != null){
                socket.close();
                handler.sendEmptyMessage(MainActivity.UdpClientHandler.UPDATE_END);
            }
        }

    }
}