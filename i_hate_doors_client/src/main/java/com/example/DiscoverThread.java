package com.example;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

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
        this.running = true;
        try {
            this.socket = new DatagramSocket(this.port, InetAddress.getByName("0.0.0.0"));
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        System.out.println("Listening for UDP discovery packet on port " + this.port + "...");
        while (this.running) {
            try {
                DatagramPacket packet = new DatagramPacket(this.buf, this.buf.length);
                this.socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                this.buf = new byte[256]; // clearing buffer
                received = received.replace("\0", "");
                System.out.println("Receiveing UDP packet...");
                if (validateJson(received)) {
                    System.out.println("Valid data!");
                    stopRunning();
                    this.mainWindow.searchEndMessage();
                    break;
                } else {
                    System.out.println("Invalid data.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validateJson(String input) {
        try {
            this.data = Tools.discoverDataFromJson(input);
            if (data.hostname != null && data.ip != null)
                return true;
            else
                return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void stopRunning() {
        this.running = false;
        this.socket.close(); // after stopping throws error, but works :)
        System.out.println("Stopped listening for UDP discovery packet.");
    }

    public DiscoverData getDiscoverData() {
        return this.data;
    }
}
