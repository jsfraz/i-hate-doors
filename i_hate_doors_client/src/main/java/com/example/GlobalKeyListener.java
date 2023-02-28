package com.example;

import java.awt.event.KeyEvent;
import java.util.regex.Matcher;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

// https://github.com/kwhat/jnativehook/blob/2.2/doc/Keyboard.md

public class GlobalKeyListener implements NativeKeyListener {
    private MainWindow mainWindow;

    public GlobalKeyListener(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        Settings settings = SettingsSingleton.GetInstance().getSettings();
        if (settings.toggleButton) {
            if (KeyEvent.getKeyText(settings.toggleKey) == NativeKeyEvent.getKeyText(e.getKeyCode())) {
                if (mainWindow.isMqttThreadNull() == false) {
                    if (mainWindow.isMqttThreadRunning()) {
                        mainWindow.stopMqttThread();
                    } else {
                        mainWindow.startMqttThread();
                    }
                } else {
                    Matcher matcher = mainWindow.ipv4Pattern.matcher(settings.ip);
                    if (matcher.matches()) {
                        mainWindow.startMqttThread();
                    }
                }
            }
        }
    }
}