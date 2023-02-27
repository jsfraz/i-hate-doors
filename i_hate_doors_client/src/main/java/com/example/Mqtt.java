package com.example;

import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class Mqtt {
    private String topic;
    private int qos;

    private MqttClient client;
    private MqttConnectOptions options;

    public Mqtt(String host, String topic) {
        this.topic = topic;
        this.qos = 0;
        try {
            this.client = new MqttClient("tcp://" + host + ":1883", UUID.randomUUID().toString(), new MemoryPersistence());
        } catch (MqttException e) {
            e.printStackTrace();
        }
        this.options = new MqttConnectOptions();
        this.options.setConnectionTimeout(1);
        this.options.setKeepAliveInterval(60);
    }

    public boolean testConnection() {
        System.out.println("Testing MQTT connection...");
        try {
            this.client.connect(options);
            this.client.disconnect();
            System.out.println("Success.");
            return true;
        } catch (MqttException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void publish(String content) throws MqttException {
        System.out.println("Publishing MQTT message...");
        this.client.setCallback(new MqttCallback() {

            public void connectionLost(Throwable cause) {
                // nothing
            }

            public void messageArrived(String topic, MqttMessage message) {
                // nothing as well
            }

            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("Success.");
            }
        });
        this.client.connect(this.options);
        // create message and setup QoS
        MqttMessage message = new MqttMessage(content.getBytes());
        message.setQos(this.qos);
        // publish message
        this.client.publish(this.topic, message);
        // disconnect
        this.client.disconnect();
        // close client
        this.client.close();
    }
}
