package com.example;

import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Settings {
    public String ip;
    public boolean lastSearchSuccess;

    public Settings() {
        this.ip = "";
        this.lastSearchSuccess = false;
    }

    public void saveSettings(String settingsFile) throws IOException {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Settings.class, new EntityJsonSerializer());
        Gson gson = builder.create();
        FileWriter writer = new FileWriter(settingsFile);
        writer.write(gson.toJson(this));
        writer.close();
    }
}
