package com.example;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.UUID;

import javax.swing.JOptionPane;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.gson.Gson;

// https://www.emqx.com/en/blog/how-to-use-mqtt-in-java

public class Mqtt {
    private String topic;
    private int qos;

    private MqttClient client;
    private MqttConnectOptions options;

    private Message doorMessage;

    public Mqtt(String host, String topic) {
        this.topic = topic;
        this.qos = 0;
        try {
            client = new MqttClient("tcp://" + host + ":1883", UUID.randomUUID().toString(), new MemoryPersistence());
        } catch (MqttException e) {
            e.printStackTrace();
        }
        MqttConnectOptions options = new MqttConnectOptions();
        options.setConnectionTimeout(1);
        options.setKeepAliveInterval(60);
    }

    public void publish(String content) throws MqttException {
        System.out.println("Publishing MQTT message...");
        client.connect(options);
        // create message and setup QoS
        MqttMessage message = new MqttMessage(content.getBytes());
        message.setQos(qos);
        // publish message
        client.publish(topic, message);
        // disconnect
        client.disconnect();
        // close client
        client.close();
        System.out.println("Success.");
    }

    public void subscribe(MainWindow mainWindow) {
        // setup callback
        client.setCallback(new MqttCallback() {

            public void connectionLost(Throwable cause) {
                String errorMessage = "Connection to MQTT broker lost: " + cause.getMessage();
                System.out.println(errorMessage);
                JOptionPane.showMessageDialog(mainWindow, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
            }

            public void messageArrived(String topic, MqttMessage message) {
                System.out.println("Receiving MQTT message...");
                String json = new String(message.getPayload());
                if (validateJson(json)) {
                    if (doorMessage.type == MessageType.doorOpened || doorMessage.type == MessageType.doorClosed) {
                        try {
                            Robot robot = new Robot();
                            Settings setings = SettingsSingleton.GetInstance().getSettings();
                            if (setings.muteOption == MuteOption.microphone) {
                                robot.keyPress(setings.muteMicKey);
                                robot.keyRelease(setings.muteMicKey);
                            }
                            if (setings.muteOption == MuteOption.sound) {
                                robot.keyPress(setings.muteSoundKey);
                                robot.keyRelease(setings.muteSoundKey);
                            }
                        } catch (AWTException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            public void deliveryComplete(IMqttDeliveryToken token) {
                // nothing
            }
        });
        try {
            System.out.println("Connecting to MQTT broker...");
            client.connect(options);
            System.out.println("Success.");
            try {
                client.subscribe(topic, qos);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } catch (MqttException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainWindow, "Unable to connect to MQTT broker: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateJson(String input) {
        try {
            this.doorMessage = new Gson().fromJson(input, Message.class);
            System.out.println("Valid data!");
            return true;
        } catch (Exception e) {
            System.out.println("Invalid data.");
            return false;
        }
    }

    public boolean testConnection() {
        System.out.println("Testing MQTT connection...");
        try {
            client.connect(options);
            client.disconnect();
            client.close();
            System.out.println("Success.");
            return true;
        } catch (MqttException e) {
            e.printStackTrace();
            return false;
        }
    }
}
