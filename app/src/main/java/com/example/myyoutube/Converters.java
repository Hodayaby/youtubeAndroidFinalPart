package com.example.myyoutube;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Converters {
    @TypeConverter
    public static List<String> fromString(String value) {
        return value != null ? Arrays.asList(value.split(";")) : new ArrayList<>();
    }

    @TypeConverter
    public static String fromList(List<String> list) {
        return list != null ? String.join(";", list) : null;
    }
}
