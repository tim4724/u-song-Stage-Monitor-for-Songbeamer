package com.tim.usong.resource;

import com.tim.usong.GlobalPreferences;
import com.tim.usong.USongApplication;
import com.tim.usong.core.SongParser;
import com.tim.usong.core.SongbeamerSettings;
import com.tim.usong.core.entity.Song;
import com.tim.usong.ui.SelectSongDirectoryDialog;
import com.tim.usong.util.AutoStartUtil;
import com.tim.usong.view.SettingsView;
import io.dropwizard.views.View;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

@Path("settings")
public class SettingsResource {
    private final SongbeamerSettings sbSettings;
    private SongParser songParser;
    private SongResource songResource;

    public SettingsResource(SongbeamerSettings sbSettings, SongParser songParser, SongResource songResource) {
        this.sbSettings = sbSettings;
        this.songParser = songParser;
        this.songResource = songResource;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public View getSettings(@Context HttpServletRequest request) {
        Locale locale = request.getLocale();
        return new SettingsView(locale, sbSettings, songParser);
    }

    @PUT
    public Response editSettings(@QueryParam("autoStart") Boolean autoStart,
                                 @QueryParam("splashScreen") Boolean showSplashScreen,
                                 @QueryParam("checkUpdates") Boolean notifyUpdates,
                                 @QueryParam("songDir") Boolean songDir,
                                 @QueryParam("titleHasPage") Boolean titleHasPage,
                                 @QueryParam("maxLinesPage") Integer maxLinesPage) throws IOException {
        if (autoStart == null && showSplashScreen == null && notifyUpdates == null && songDir == null
                && titleHasPage == null && maxLinesPage == null) {
            return Response.status(400).build();
        }

        if (autoStart != null) {
            if (autoStart) {
                AutoStartUtil.enableAutoStart(USongApplication.getCurrentJarPath());
            } else {
                AutoStartUtil.disableAutoStart();
            }
        }
        if (showSplashScreen != null) {
            GlobalPreferences.setShowSplashScreen(showSplashScreen);
        }
        if (notifyUpdates != null) {
            GlobalPreferences.setNotifyUpdates(notifyUpdates);
        }

        if (songDir != null) {
            File newSongDir = new SelectSongDirectoryDialog().getDirectory();
            if (newSongDir != null) {
                GlobalPreferences.setSongDir(newSongDir.getAbsolutePath());
                songParser.setSongDir(newSongDir);
                reloadSong();
            }
        }
        if (titleHasPage != null) {
            GlobalPreferences.setTitleHasPage(titleHasPage);
            songParser.setTitleHasOwnPage(titleHasPage);
            reloadSong();
        }
        if (maxLinesPage != null) {
            GlobalPreferences.setMaxLinesPage(maxLinesPage);
            songParser.setMaxLinesPerPage(maxLinesPage);
            reloadSong();
        }
        return Response.ok().build();
    }

    private void reloadSong() {
        Song currentSong = songResource.getSong();
        String songFileName;
        if (currentSong != null && (songFileName = currentSong.getFileName()) != null) {
            songResource.setSong(songFileName);
        }
    }
}