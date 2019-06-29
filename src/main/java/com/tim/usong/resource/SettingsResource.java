package com.tim.usong.resource;

import com.tim.usong.GlobalPreferences;
import com.tim.usong.USongApplication;
import com.tim.usong.core.SongParser;
import com.tim.usong.core.SongbeamerSettings;
import com.tim.usong.core.entity.Song;
import com.tim.usong.ui.FullScreenStageMonitor;
import com.tim.usong.ui.SelectSongDirectoryDialog;
import com.tim.usong.util.AutoStart;
import com.tim.usong.util.SongbeamerUpdateChecker;
import com.tim.usong.util.UpdateChecker;
import com.tim.usong.view.SettingsView;
import io.dropwizard.views.View;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

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
                                 @QueryParam("checkSongbeamerUpdates") Boolean notifySongbeamerUpdates,
                                 @QueryParam("showClockInSong") Boolean showClockInSong,
                                 @QueryParam("songDir") Boolean songDir,
                                 @QueryParam("titleHasPage") Boolean titleHasPage,
                                 @QueryParam("maxLinesPage") Integer maxLinesPage,
                                 @QueryParam("chords") Boolean showChords,
                                 @QueryParam("showOnDisplay") Integer showOnDisplay) {
        if (autoStart == null && showSplashScreen == null && notifyUpdates == null && songDir == null
                && titleHasPage == null && maxLinesPage == null && notifySongbeamerUpdates == null
                && showClockInSong == null && showChords == null && showOnDisplay == null) {
            return Response.status(400).entity("Fehler").build();
        }

        if (autoStart != null) {
            try {
                if (autoStart) {
                    AutoStart.enableAutoStart(USongApplication.getCurrentJarPath());
                } else {
                    AutoStart.disableAutoStart();
                }
            } catch (Exception e) {
                ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
                return Response.status(500).entity(messages.getString("autostartChangeFailed")).build();
            }
        }
        if (showSplashScreen != null) {
            GlobalPreferences.setShowSplashScreen(showSplashScreen);
        }
        if (showClockInSong != null) {
            GlobalPreferences.setShowClockInSong(showClockInSong);
            songResource.forceClientReload();
        }
        if (notifyUpdates != null) {
            GlobalPreferences.setNotifyUpdates(notifyUpdates);
            if (notifyUpdates) {
                UpdateChecker.checkForUpdateAsync();
            }
        }
        if (notifySongbeamerUpdates != null) {
            GlobalPreferences.setNotifySongbamerUpdates(notifySongbeamerUpdates);
            if (notifySongbeamerUpdates && sbSettings.version != null) {
                SongbeamerUpdateChecker.checkForUpdateAsync(sbSettings.version);
            }
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
        if (showChords != null) {
            GlobalPreferences.setShowChords(showChords);
            reloadSong();
        }
        if (showOnDisplay != null) {
            try {
                if (showOnDisplay >= 0) {
                    GlobalPreferences.setFullscreenDisplay(showOnDisplay);
                    FullScreenStageMonitor.showOnDisplay(showOnDisplay);
                } else {
                    GlobalPreferences.setFullscreenDisplay(-1);
                    FullScreenStageMonitor.close();
                }
            } catch (Exception e) {
                ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
                return Response.status(500).entity(messages.getString("showFullscreenError")).build();
            }
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
