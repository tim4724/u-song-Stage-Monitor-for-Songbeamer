package com.tim.usong.view;

import com.tim.usong.GlobalPreferences;
import com.tim.usong.core.entity.Song;
import io.dropwizard.views.View;

import java.util.Locale;
import java.util.ResourceBundle;

public class SongView extends View {
    private final ResourceBundle messages;
    private final Song song;
    private final boolean showPageNumbers;
    private final boolean admin;
    private final boolean chords;

    public SongView(Song song, Locale locale, boolean admin, Boolean chords, boolean showPageNumbers) {
        super("song.ftl");
        if (chords == null) {
            chords = GlobalPreferences.getShowChords();
        }
        this.messages = ResourceBundle.getBundle("MessagesBundle", locale);
        this.song = song;
        this.admin = admin;
        this.chords = chords;
        this.showPageNumbers = showPageNumbers;
    }

    public ResourceBundle getMessages() {
        return messages;
    }

    public Song getSong() {
        return song;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isShowPageNumbers() {
        return showPageNumbers;
    }

    public boolean isChords() {
        return chords;
    }

    public boolean isShowClockInSong() {
        return GlobalPreferences.isShowClockInSong();
    }
}