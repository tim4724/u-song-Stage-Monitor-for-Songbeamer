package com.tim.usong.resource;

import com.google.common.base.Strings;
import com.tim.usong.USongApplication;
import com.tim.usong.core.SongParser;
import com.tim.usong.core.SongbeamerListener;
import com.tim.usong.core.entity.Section;
import com.tim.usong.core.entity.Song;
import com.tim.usong.util.AutoStartUtil;
import com.tim.usong.view.StatusView;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

@Path("status")
public class StatusResource {
    private SongbeamerListener songbeamerListener;
    private SongResource songResource;
    private SongParser songParser;
    private USongApplication.SongBeamerSettings settings;

    public StatusResource(SongbeamerListener songbeamerListener,
                          SongResource songResource,
                          SongParser songParser,
                          USongApplication.SongBeamerSettings settings) {
        this.songbeamerListener = songbeamerListener;
        this.songResource = songResource;
        this.songParser = songParser;
        this.settings = settings;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public StatusView getStatus(@Context HttpServletRequest request) {
        Locale locale = request.getLocale();
        Status status = new Status(locale, songbeamerListener, songResource, songParser, settings);
        return new StatusView(locale, status);
    }


    public class Status {
        private final String version, hostname, ipAddress, sbVersion, songDir, songTitle, currentSection;
        private final boolean startWithWindows, connected;
        private final int clientCount, songCount, currentPage, lang, langCount;

        public Status(Locale locale,
                      SongbeamerListener songbeamerListener,
                      SongResource songResource,
                      SongParser songParser,
                      USongApplication.SongBeamerSettings songBeamerSettings) {
            ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle", locale);
            Song song = songResource.getSong();

            this.version = USongApplication.APP_VERSION;
            this.startWithWindows = AutoStartUtil.isAutostartEnabled();
            this.clientCount = songResource.getClientCount();
            this.hostname = getHostname(messages.getString("unknown"));
            this.ipAddress = getHostAddress(messages.getString("unknown"));
            this.sbVersion = songBeamerSettings.version;
            this.connected = songbeamerListener.isConnected();
            this.songDir = songParser.getSongDir();
            this.songCount = (int) countSongs(songDir);
            this.songTitle = song.getTitle();
            this.currentPage = songResource.getPage();
            this.currentSection = getSectionName(song, currentPage);
            this.lang = song.getLang();
            this.langCount = song.getLang();
        }


        private String getHostname(String defaultValue) {
            try {
                return InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                return defaultValue;
            }
        }

        private String getHostAddress(String defaultValue) {
            try {
                try (DatagramSocket socket = new DatagramSocket()) {
                    socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                    return socket.getLocalAddress().getHostAddress();
                } catch (SocketException e) {
                    return InetAddress.getLocalHost().getHostAddress();
                }
            } catch (UnknownHostException e) {
                return defaultValue;
            }
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
    }
}