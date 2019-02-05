package com.tim.usong.view;

import com.tim.usong.GlobalPreferences;
import com.tim.usong.core.SongParser;
import com.tim.usong.core.SongbeamerSettings;
import com.tim.usong.util.AutoStartUtil;
import io.dropwizard.views.View;

import java.util.Locale;
import java.util.ResourceBundle;

public class SettingsView extends View {
    private final ResourceBundle messages;
    private final SongParser songParser;
    private final SongbeamerSettings sbSettings;

    public SettingsView(Locale locale, SongbeamerSettings sbSettings, SongParser songParser) {
        super("settings.ftl");
        this.messages = ResourceBundle.getBundle("MessagesBundle", locale);
        this.sbSettings = sbSettings;
        this.songParser = songParser;
    }

    public boolean isAutostartEnabled() {
        return AutoStartUtil.isAutostartEnabled();
    }

    public boolean isShowSplash() {
        return GlobalPreferences.isShowSplashScreen();
    }

    public boolean isNotifyUpdates() {
        return GlobalPreferences.isNotifyUpdates();
    }

    public String getSongDir() {
        return songParser.getSongDirPath();
    }

    public boolean isTitleOwnPage() {
        return songParser.isTitleHasOwnPage();
    }

    public int getMaxLinesPage() {
        return songParser.getMaxLinesPerPage();
    }

    public ResourceBundle getMessages() {
        return messages;
    }

    public boolean isAllowSetSongDir() {
        return sbSettings.songDir == null;
    }

    public boolean isAllowSetTitleHasOwnPage() {
        return sbSettings.titleHasOwnPage == null;
    }

    public boolean isAllowSetMaxLinesPerPage() {
        return sbSettings.maxLinesPerPage == null;
    }
}