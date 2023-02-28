package com.example;

import java.io.BufferedInputStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javazoom.jl.player.Player;

public class Tools {
    public static String objectToJson(Object obj) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(obj.getClass(), new EntityJsonSerializer());
        Gson gson = builder.create();
        return gson.toJson(obj);
    }

    public static Settings settingsFromJson(String json) {
        return new Gson().fromJson(json, Settings.class);
    }

    public static DiscoverData discoverDataFromJson(String json) {
        return new Gson().fromJson(json, DiscoverData.class);
    }

    public static Message messageFromJson(String json) {
        return new Gson().fromJson(json, Message.class);
    }

    // https://github.com/eugenp/tutorials/blob/master/javax-sound/src/main/java/com/baeldung/SoundPlayerUsingJavaZoom.java
    public static void playSound(Sound sound) {
        try {
            BufferedInputStream buffer = new BufferedInputStream(
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(sound + ".mp3"));
            Player mp3Player = new Player(buffer);
            mp3Player.play();

        } catch (Exception ex) {
            System.out.println("Error occured during playback process:" + ex.getMessage());
        }
    }
}
