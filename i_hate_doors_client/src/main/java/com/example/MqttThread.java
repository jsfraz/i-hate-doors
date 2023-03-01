package com.example;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.swing.JOptionPane;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

// https://www.emqx.com/en/blog/how-to-use-mqtt-in-java

public class MqttThread extends Thread {
    private String topic;
    private int qos;

    private MqttClient client;
    private MqttConnectOptions options;

    private MainWindow mainWindow;
    private Message doorMessage;
    private boolean running;

    public MqttThread(String host, String topic, MainWindow mainWindow) {
        this.topic = topic;
        this.qos = 0;
        try {
            this.client = new MqttClient("tcp://" + host + ":1883", UUID.randomUUID().toString(),
                    new MemoryPersistence());
        } catch (MqttException e) {
            e.printStackTrace();
        }
        this.options = new MqttConnectOptions();
        this.options.setConnectionTimeout(1);
        this.options.setKeepAliveInterval(60);
        this.mainWindow = mainWindow;
    }

    public void run() {
        this.running = true;
        // setup callback
        this.client.setCallback(new MqttCallback() {

            public void connectionLost(Throwable cause) {
                String errorMessage = "Connection to MQTT broker lost: " + cause.getMessage();
                System.out.println(errorMessage);
                stopRunning();
                JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
            }

            public void messageArrived(String topic, MqttMessage message) {
                System.out.println("Receiving MQTT message...");
                String json = new String(message.getPayload());
                if (validateJson(json)) {
                    System.out.println("Valid message!");
                    if (doorMessage.type == MessageType.doorOpened || doorMessage.type == MessageType.doorClosed) {
                        try {
                            Robot robot = new Robot();
                            Settings setings = SettingsSingleton.GetInstance().getSettings();
                            if (setings.muteOption == MuteOption.microphone) {
                                System.out.println("Pressing " + KeyEvent.getKeyText(setings.muteMicKey) + "...");
                                robot.keyPress(setings.muteMicKey);
                                robot.keyRelease(setings.muteMicKey);
                            }
                            if (setings.muteOption == MuteOption.sound) {
                                System.out.println("Pressing " + KeyEvent.getKeyText(setings.muteSoundKey) + "...");
                                robot.keyPress(setings.muteSoundKey);
                                robot.keyRelease(setings.muteSoundKey);
                            }
                        } catch (AWTException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.out.println("Invalid message.");
                }
            }

            public void deliveryComplete(IMqttDeliveryToken token) {
                // nothing
            }
        });
        try {
            System.out.println("Connecting to MQTT broker...");
            this.client.connect(this.options);
            System.out.println("Connected.");
            this.mainWindow.setOnOffButtonStatus(Status.on);
            try {
                this.client.subscribe(this.topic, this.qos);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } catch (MqttException e) {
            e.printStackTrace();
            stopRunning();
            JOptionPane.showMessageDialog(null, "Unable to connect to MQTT broker: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        while (this.running) {
            // fake thread :)
            LocalDateTime.now();
            // interestingly, when there is not method in loop, it never ends even if
            // running is false
        }
        System.out.println("MQTT thread stopped.");
    }

    private boolean validateJson(String input) {
        try {
            this.doorMessage = Tools.messageFromJson(input);
            if (doorMessage.type != null)
                return true;
            else
                return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void stopRunning() {
        System.out.println("Disconnecting from MQTT broker...");
        this.running = false;
        /*
         * try {
         * this.client.unsubscribe(this.topic);
         * } catch (MqttException e) {
         * e.printStackTrace();
         * }
         */
        try {
            this.client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        System.out.println("Disconnected.");
        this.mainWindow.setOnOffButtonStatus(Status.off);
    }
}
