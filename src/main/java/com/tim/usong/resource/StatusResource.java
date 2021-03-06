package com.tim.usong.resource;

import com.google.common.base.Strings;
import com.tim.usong.GlobalPreferences;
import com.tim.usong.USongApplication;
import com.tim.usong.core.SongParser;
import com.tim.usong.core.SongbeamerActionListener;
import com.tim.usong.core.entity.Section;
import com.tim.usong.core.entity.Song;
import com.tim.usong.ui.FullScreenStageMonitor;
import com.tim.usong.ui.PreviewFrame;
import com.tim.usong.util.AutoStart;
import com.tim.usong.util.NetworkHost;
import com.tim.usong.view.StatusView;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

@Path("status")
public class StatusResource {
    private final SongbeamerActionListener songbeamerListener;
    private final SongResource songResource;
    private final SongParser songParser;
    private final String songbeamerVersion;

    public StatusResource(SongbeamerActionListener songbeamerListener,
                          SongResource songResource,
                          SongParser songParser,
                          String songbeamerVersion) {
        this.songbeamerListener = songbeamerListener;
        this.songResource = songResource;
        this.songParser = songParser;
        this.songbeamerVersion = songbeamerVersion;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public StatusView getStatus(@Context HttpServletRequest request) {
        Locale locale = request.getLocale();
        Status status = new Status(locale);
        return new StatusView(locale, status);
    }

    public class Status {
        private final String version, hostname, ipAddress, sbVersion, songDir, songTitle, currentSection;
        private final boolean startWithWindows, connected, preview, titelHasOwnPage, songHasChords;
        private final int clientCount, songCount, currentPage, lang, langCount, maxLinesPerPage, fullscreenDisplay;

        public Status(Locale locale) {
            ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle", locale);
            Song song = songResource.getSong();
            String hostname = NetworkHost.getHostname();
            String ipAddress = NetworkHost.getHostAddress();
            this.version = USongApplication.APP_VERSION;
            this.startWithWindows = AutoStart.isAutostartEnabled();
            this.clientCount = songResource.getClientCount();
            this.preview = PreviewFrame.isVisible();
            this.hostname = hostname != null ? hostname : messages.getString("unknown");
            this.ipAddress = ipAddress != null ? ipAddress : messages.getString("unknown");
            this.sbVersion = songbeamerVersion;
            this.connected = songbeamerListener.isConnected();
            this.songDir = songParser.getSongDirPath();
            this.songCount = (int) countSongs(songDir);
            this.songTitle = song.getTitle();
            this.currentPage = songResource.getPage();
            this.currentSection = getSectionName(song, currentPage);
            this.lang = song.getLang();
            this.langCount = song.getLangCount();
            this.titelHasOwnPage = songParser.isTitleHasOwnPage();
            this.maxLinesPerPage = songParser.getMaxLinesPerPage();
            this.songHasChords = song.hasChords();
            boolean isDisplaying = FullScreenStageMonitor.isDisplaying();
            this.fullscreenDisplay = isDisplaying ? GlobalPreferences.getFullscreenDisplay() : -1;
        }

        private long countSongs(String songPath) {
            String[] files = new File(songPath).list();
            return files != null ? Arrays.stream(files).filter(s -> s.endsWith(".sng")).count() : 0;
        }

        private String getSectionName(Song song, int page) {
            if (page < 0) {
                return null;
            }
            for (Section section : song.getSections()) {
                page -= section.getPages().size();
                if (page < 0) {
                    return Strings.emptyToNull(section.getName());
                }
            }
            return null;
        }

        public String getVersion() {
            return version;
        }

        public String getHostname() {
            return hostname;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public String getSbVersion() {
            return sbVersion;
        }

        public String getSongDir() {
            return songDir;
        }

        public String getSongTitle() {
            return songTitle;
        }

        public String getCurrentSection() {
            return currentSection;
        }

        public boolean isStartWithWindows() {
            return startWithWindows;
        }

        public boolean isConnected() {
            return connected;
        }

        public int getClientCount() {
            return clientCount;
        }

        public int getSongCount() {
            return songCount;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public int getLang() {
            return lang;
        }

        public int getLangCount() {
            return langCount;
        }

        public boolean isPreview() {
            return preview;
        }

        public boolean isTitelHasOwnPage() {
            return titelHasOwnPage;
        }

        public int getMaxLinesPerPage() {
            return maxLinesPerPage;
        }

        public boolean isSongHasChords() {
            return songHasChords;
        }

        public int getFullscreenDisplay() {
            return fullscreenDisplay;
        }
    }
}