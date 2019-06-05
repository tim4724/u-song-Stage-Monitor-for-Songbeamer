package com.tim.usong;

import java.util.Arrays;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class GlobalPreferences {
    private static final Preferences preferences =
            Preferences.userNodeForPackage(GlobalPreferences.class).node("preferences");

    private GlobalPreferences() {
    }

    public static void setShowSplashScreen(boolean show) {
        preferences.putBoolean("splashScreen", show);
    }

    public static boolean isShowSplashScreen() {
        return preferences.getBoolean("splashScreen", true);
    }

    public static void setNotifyUpdates(boolean notify) {
        preferences.putBoolean("notifyUpdates", notify);
    }

    public static boolean isNotifyUpdates() {
        return preferences.getBoolean("notifyUpdates", true);
    }

    public static void setNotifySongbamerUpdates(boolean notify) {
        preferences.putBoolean("notifyUpdatesSongbeamer", notify);
    }

    public static boolean isNotifySongbeamerUpdates() {
        return preferences.getBoolean("notifyUpdatesSongbeamer", false);
    }

    public static void setShowClockInSong(boolean show) {
        preferences.putBoolean("showClockInSong", show);
    }

    public static boolean isShowClockInSong() {
        return preferences.getBoolean("showClockInSong", true);
    }

    public static void setSongDir(String songDir) {
        preferences.put("songDir", songDir);
    }

    public static String getSongDir() {
        return preferences.get("songDir", null);
    }

    public static void setTitleHasPage(boolean titleOwnPage) {
        preferences.putBoolean("titleHasPage", titleOwnPage);
    }

    public static boolean hasTitlePage() {
        return preferences.getBoolean("titleHasPage", false);
    }

    public static void setMaxLinesPage(int maxLines) {
        preferences.putInt("maxLinesPage", maxLines);
    }

    public static int getMaxLinesPage() {
        return preferences.getInt("maxLinesPage", 0);
    }

    public static void setShowChords(boolean chords) {
        preferences.putBoolean("chords", chords);
    }

    public static boolean getShowChords() {
        return preferences.getBoolean("chords", false);
    }

    public static Boolean getShowChordsOrNull() {
        try {
            if (!Arrays.asList(preferences.keys()).contains("chords")) {
                return null;
            }
        } catch (BackingStoreException e) {
            return null;
        }
        return getShowChords();
    }
}
