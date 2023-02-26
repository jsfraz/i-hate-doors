package com.example;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.google.gson.Gson;

//https://www.baeldung.com/udp-in-java

public class DiscoverThread extends Thread {
    private int port = 52375;
    private DatagramSocket socket;
    private byte[] buf = new byte[256];

    private MainWindow mainWindow;
    private DiscoverData data;
    private boolean running;

    public DiscoverThread(MainWindow mainWidow) {
        this.mainWindow = mainWidow;
        running = true;
        try {
            socket = new DatagramSocket(port, InetAddress.getByName("0.0.0.0"));
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        System.out.println("Listening for UDP discovery packet on port " + port + "...");
        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                buf = new byte[256]; // clearing buffer
                received = received.replace("\0", "");
                System.out.println("Receiveing UDP packet...");
                if (validateJson(received)) {
                    stopRunning();
                    mainWindow.searchEndMessage();
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validateJson(String input) {
        try {
            data = new Gson().fromJson(input, DiscoverData.class);
            System.out.println("Valid data!");
            return true;
        } catch (Exception e) {
            System.out.println("Invalid data.");
            return false;
        }
    }

    public void stopRunning() {
        running = false;
        socket.close();     // after stopping throws error, but works :)
        System.out.println("Stopped listening for UDP discovery packet.");
    }

    public DiscoverData getDisoverData() {
        return data;
    }
}
