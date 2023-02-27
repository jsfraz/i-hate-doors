package com.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
}
