package com.tim.usong.view;

import com.tim.usong.core.entity.Song;
import io.dropwizard.views.View;

public class SongView extends View {
    private final Song song;
    private final boolean admin;

    public SongView(Song song, boolean admin) {
        super("song.ftl");
        this.song = song;
        this.admin = admin;
    }

    public Song getSong() {
        return song;
    }

    public boolean isAdmin() {
        return admin;
    }
}