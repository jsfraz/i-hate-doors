package com.example;

import java.io.FileWriter;
import java.io.IOException;

public class Settings {
    public String ip;
    public MuteOption muteOption;
    public int muteMicKey;
    public int muteSoundKey;
    public boolean toggleButton;
    public int toggleKey;
    public boolean playToggleSound;

    public Settings() {
        this.ip = "";
        this.muteOption = MuteOption.microphone;
        this.muteMicKey = 117;
        this.muteSoundKey = 118;
        this.toggleButton = true;
        this.toggleKey = 119;
        this.playToggleSound = true;
    }

    public void saveSettings(String settingsFile) throws IOException {
        FileWriter writer = new FileWriter(settingsFile);
        writer.write(Tools.objectToJson(this));
        writer.close();
    }
}
