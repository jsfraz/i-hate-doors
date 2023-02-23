package com.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import com.google.gson.Gson;

public class SettingsSingleton {
    private static SettingsSingleton instance;
    private static final String settingsFile = System.getProperty("user.dir") + "/settings.json";
    private Settings settings;

    private SettingsSingleton() {
        this.settings = new Settings();
    }

    public static SettingsSingleton GetInstance() {
        if (instance == null) {
            // checks if file exists
            if (new File(settingsFile).exists()) {
                instance = new SettingsSingleton();
                // read file
                try {
                    System.out.println("Reading settings from file...");
                    String json = readSettingsJson();
                    instance.settings = new Gson().fromJson(json, Settings.class);
                    System.out.println("Success.");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                instance = new SettingsSingleton();
                // create file
                try {
                    System.out.println("Creating new settings file...");
                    instance.settings.saveSettings(settingsFile);
                    System.out.println("Success.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return instance;
    }

    // https://www.geeksforgeeks.org/different-ways-reading-text-file-java/
    private static String readSettingsJson() throws FileNotFoundException {
        File file = new File(settingsFile);
        Scanner sc = new Scanner(file);
        String result = "";
        while (sc.hasNextLine())
            result += sc.nextLine();
        sc.close();
        return result;
    }

    public void saveSettings() throws IOException {
        instance.settings.saveSettings(settingsFile);
    }

    public String getIp() {
        return this.settings.ip;
    }

    public void setIp(String ip) {
        this.settings.ip = ip;
    }

    public boolean getLastSearchSuccessful() {
        return this.settings.lastSearchSuccess;
    }

    public void setLastSearchSuccessful(boolean value) {
        this.settings.lastSearchSuccess = value;
    }
}
