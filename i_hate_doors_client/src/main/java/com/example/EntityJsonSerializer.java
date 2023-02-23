package com.example;

import java.lang.reflect.Type;

import com.google.gson.*;

// https://stackoverflow.com/questions/13175019/custom-conversion-from-java-object-to-json-object

public class EntityJsonSerializer implements JsonSerializer<Object> {
    @Override
    public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context) {
        Gson gson = new Gson();
        return gson.toJsonTree(src);
    }
}