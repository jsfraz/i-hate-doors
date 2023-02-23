package com.example;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.google.gson.Gson;

//https://www.baeldung.com/udp-in-java

public class DiscoverHandler {
    private int port = 52375;
    private DatagramSocket socket;
    private byte[] buf = new byte[256];

    private JFrame parentFrame;
    private JDialog searchDialog;
    private JTextField ipField;
    private JButton findButton;
    private String oldIp;

    public DiscoverHandler(JFrame parentFrame, JDialog searchDialog, JTextField ipField, JButton findButton) {
        this.parentFrame = parentFrame;
        this.searchDialog = searchDialog;
        this.ipField = ipField;
        this.findButton = findButton;

        oldIp = SettingsSingleton.GetInstance().getIp();

        try {
            socket = new DatagramSocket(port, InetAddress.getByName("0.0.0.0"));
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        System.out.println("Listening for discovery packet on port " + port + "...");
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                buf = new byte[256]; // clearing buffer
                received = received.replace("\0", "");
                System.out.println("Received discovery packet...");
                if (validateJson(received)) {
                    // TODO send packet for end
                    break;
                } else {
                    System.out.println("Invalid data.");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        socket.close();
        finishMessage();
    }

    private boolean validateJson(String input) {
        try {
            DiscoverData data = new Gson().fromJson(input, DiscoverData.class);
            System.out.println("Valid data!");
            System.out.println("Adding " + data.hostname + " (" + data.ip + ").");
            SettingsSingleton.GetInstance().setIp(data.ip);
            SettingsSingleton.GetInstance().setLastSearchSuccessful(true);
            try {
                SettingsSingleton.GetInstance().saveSettings();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        } catch (Exception e) {
            SettingsSingleton.GetInstance().setLastSearchSuccessful(false);
            return false;
        }
    }

    private void finishMessage() {
        searchDialog.dispose();
        ipField.setText(SettingsSingleton.GetInstance().getIp());
        findButton.setEnabled(true);

        String message = "";
        String title = "";
        int messageType = 0;
        if (SettingsSingleton.GetInstance().getLastSearchSuccessful()) {
            if (oldIp.equals(SettingsSingleton.GetInstance().getIp())) {
                message = "IP address wasn't changed.";
                title = "Warning";
                messageType = JOptionPane.WARNING_MESSAGE;
            } else {
                message = "Found!";
                title = "Info";
                messageType = JOptionPane.INFORMATION_MESSAGE;
            }
        } else {
            message = "Device was not found.";
            title = "Error";
            messageType = JOptionPane.ERROR_MESSAGE;
        }
        JOptionPane.showMessageDialog(parentFrame, message, title, messageType);
    }
}
