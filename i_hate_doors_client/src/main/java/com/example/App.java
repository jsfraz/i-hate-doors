package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class App {
    public static void main(String[] args) throws IOException {
        /*
         * splash text
         * ASCII text generator:
         * https://patorjk.com/software/taag/#p=display&f=Big&t=I%20hate%20doors!
         * ASCII art: https://ascii.co.uk/art/doors
         * file from resource folder:
         * https://stackoverflow.com/questions/15749192/how-do-i-load-a-file-from-
         * resource-folder
         * InputStream to String:
         * https://www.baeldung.com/convert-input-stream-to-string
         */
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("splash.txt");
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(
                new InputStreamReader(inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        }
        System.out.println(textBuilder.toString());
        System.out.println("Starting...");

        // settings singleton
        SettingsSingleton.GetInstance();

        // main window
        new MainWindow();
    }
}
