package com.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

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
                    instance.settings = Tools.settingsFromJson(json);
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
        System.out.println("Saving settings file...");
        instance.settings.saveSettings(settingsFile);
        System.out.println("Success.");
    }

    public String getIp() {
        return this.settings.ip;
    }

    public void setIp(String ip) {
        this.settings.ip = ip;
    }

    public MuteOption getMuteOption() {
        return this.settings.muteOption;
    }

    public void setMuteOption(MuteOption value) {
        this.settings.muteOption = value;
    }

    public int getMuteMicKey() {
        return this.settings.muteMicKey;
    }

    public void setMuteMicKey(int value) {
        this.settings.muteMicKey = value;
    }

    public int getMuteSoundKey() {
        return this.settings.muteSoundKey;
    }

    public void setMuteSoundKey(int value) {
        this.settings.muteSoundKey = value;
    }

    public boolean getToggleButton() {
        return this.settings.toggleButton;
    }

    public void setToggleButton(boolean value) {
        this.settings.toggleButton = value;
    }

    public int getToggleKey() {
        return this.settings.toggleKey;
    }

    public void setToggleKey(int value) {
        this.settings.toggleKey = value;
    }

    public Settings getSettings() {
        return this.settings;
    }
}
