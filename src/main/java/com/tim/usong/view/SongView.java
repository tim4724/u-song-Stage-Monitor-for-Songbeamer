package com.tim.usong.view;

import com.tim.usong.core.entity.Song;
import io.dropwizard.views.View;

import java.util.Locale;
import java.util.ResourceBundle;

public class SongView extends View {
    private final ResourceBundle messages;
    private final Song song;
    private final boolean admin;

    public SongView(Song song, Locale locale, boolean admin) {
        super("song.ftl");
        this.messages = ResourceBundle.getBundle("MessagesBundle", locale);
        this.song = song;
        this.admin = admin;
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
}