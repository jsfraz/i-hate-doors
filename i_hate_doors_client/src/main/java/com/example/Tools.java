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
}
