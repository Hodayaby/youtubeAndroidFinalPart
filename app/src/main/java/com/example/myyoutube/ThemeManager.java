package com.example.myyoutube;


public class ThemeManager {
    private static boolean isDarkMode = false;

    public static boolean isDarkMode() {
        return isDarkMode;
    }

    public static void setDarkMode(boolean isDarkMode) {
        ThemeManager.isDarkMode = isDarkMode;
    }
}

