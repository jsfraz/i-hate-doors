package com.example;

import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

// https://www.emqx.com/en/blog/how-to-use-mqtt-in-java

public class MqttHandler {
    private String topic;
    private int qos;

    private MqttClient client;
    private MqttConnectOptions options;

    public MqttHandler(String host, String topic) {
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

    public void subscribe() {
        try {
            // setup callback
            client.setCallback(new MqttCallback() {

                public void connectionLost(Throwable cause) {
                    System.out.println("Connection lost: " + cause.getMessage());
                    // TODO user notification
                }

                public void messageArrived(String topic, MqttMessage message) {
                    System.out.println("Receiving MQTT message...");
                    // TODO receive mqtt message
                    new String(message.getPayload());
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                    // nothing
                }
            });
            client.connect(options);
            client.subscribe(topic, qos);
        } catch (Exception e) {
            e.printStackTrace();
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
