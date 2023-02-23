package com.example;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

//https://www.baeldung.com/udp-in-java

public class UdpBroadcastServer extends Thread {

    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[256];

    public UdpBroadcastServer() {
        try {
            socket = new DatagramSocket(52375, InetAddress.getByName("0.0.0.0"));
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        LocalDateTime started = LocalDateTime.now();
        running = true;

        while (running) {
            running = !LocalDateTime.now().isAfter(started.plusSeconds(5));

            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(buf, buf.length, address, port);
                String received = new String(packet.getData(), 0, packet.getLength());

                // vyčištění bufferu
                buf = new byte[256];
                received = received.replace("\0", "");

                System.out.println(received);
                /*
                 * if (received == "end") {
                 * running = false;
                 * continue;
                 * }
                 */
            } catch (IOException e) {
                // TODO Auto-generated catch block
                running = false;
            }
        }
        socket.close();
    }
}
