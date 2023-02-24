package com.example;

import java.io.FileWriter;
import java.io.IOException;

public class Settings {
    public String ip;
    public boolean lastSearchSuccess;

    public Settings() {
        this.ip = "";
        this.lastSearchSuccess = false;
    }

    public void saveSettings(String settingsFile) throws IOException {
        FileWriter writer = new FileWriter(settingsFile);
        writer.write(Tools.objectToJson(this));
        writer.close();
    }
}
