package com.fruitoftek.androidfaceattendance.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class JsonObjectMapperSingleton {
    private static ObjectMapper objectMapper;
    private static ObjectWriter objectWriter;
    private static ObjectReader objectReader;

    static {
        objectMapper = new ObjectMapper();
        objectWriter = objectMapper.writer();
        objectReader = objectMapper.reader();
    }

    private JsonObjectMapperSingleton() {
        // Doing this constructor private since we don't want this class getting instantiated
    }

    public static ObjectWriter getObjectWriter() {
        return objectWriter;
    }

    public static ObjectReader getObjectReader() {
        return objectReader;
    }
}
