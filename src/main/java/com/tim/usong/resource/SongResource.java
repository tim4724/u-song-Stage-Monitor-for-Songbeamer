package com.tim.usong.resource;

import com.tim.usong.GlobalPreferences;
import com.tim.usong.core.SongParser;
import com.tim.usong.core.entity.Song;
import com.tim.usong.ui.FullScreenStageMonitor;
import com.tim.usong.view.SongView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

@Path("song")
public class SongResource {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
    private final SongParser songParser;
    private Song song;
    private Song nextSong;

    public SongResource(SongParser songParser) {
        this.songParser = songParser;
        song = new Song(messages.getString("waitingForSongbeamer"), Song.Type.INFO);
        SongWebSocket.songId = song.hashCode();
        SongWebSocket.songType = song.getType();
        SongWebSocket.page = -1;
    }

    public void setSongAndPage(Song song, int page) {
        setSong(song);
        setPage(page);
    }

    public void setSong(String songFilename) {
        if (!songFilename.endsWith(".sng")) {
            String title = messages.getString("noSongSelected");
            if (!songFilename.isEmpty()) {
                title += "<br>\n" + songFilename;
            }
            setSong(new Song(title, Song.Type.INFO));
        } else {
            setSong(songParser.parse(songFilename));
        }

        int showOnDisplay = GlobalPreferences.getFullscreenDisplay();
        if (showOnDisplay != -1 && !FullScreenStageMonitor.isDisplaying()) {
            try {
                FullScreenStageMonitor.showOnDisplay(showOnDisplay);
            } catch (Exception e) {
                logger.error("Failed to display in fullscreen mode", e);
            }
        }
    }

    public void setSong(Song newSong) {
        boolean sameFileAsCurrent = newSong.getFileName() != null
                && newSong.getFileName().equals(this.song.getFileName());

        if (this.song == null
                // Nothing is displayed on the beamer and new Song is of type "SNG"
                || (SongWebSocket.page == -1 && newSong.getType() == Song.Type.SNG)
                // Current song is not of Type "SNG", therefore not important and we can switch
                || this.song.getType() != Song.Type.SNG
                // Same file as current, i.e. the song-text could have been edit, therefore switch
                || sameFileAsCurrent) {
            this.nextSong = null;
            this.song = newSong;
            SongWebSocket.songId = newSong.hashCode();
            SongWebSocket.songType = newSong.getType();
            if (!sameFileAsCurrent) {
                SongWebSocket.page = -1;
            }
            SongWebSocket.notifyDataChanged();
        } else {
            // Sometimes we want to show the current song longer
            // Switch later to "nextSong"
            this.nextSong = newSong;
        }
    }

    public void setPage(int page) {
        if (this.nextSong != null && (this.nextSong.getType() == Song.Type.SNG || page >= 0)) {
            this.song = nextSong;
            this.nextSong = null;
            SongWebSocket.songId = this.song.hashCode();
            SongWebSocket.songType = this.song.getType();
        }
        SongWebSocket.page = page;
        SongWebSocket.notifyDataChanged();
    }

    void forceClientReload() {
        int currentSongId = SongWebSocket.songId;
        SongWebSocket.songId = 0;
        SongWebSocket.notifyDataChanged();
        SongWebSocket.songId = currentSongId;
        SongWebSocket.notifyDataChanged();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public SongView getSong(@Context HttpServletRequest request,
                            @QueryParam("admin") boolean admin,
                            @QueryParam("chords") Boolean chords) {
        return new SongView(song, nextSong, request.getLocale(), admin, chords);
    }

    @POST
    @Path("page/{newPage}")
    public Response setPageForCurrentSong(@PathParam("newPage") int newPage) {
        if (newPage < -1 || newPage >= song.getPageCount()) {
            return Response.status(400).build();
        }
        setPage(newPage);
        return Response.ok().build();
    }

    @POST
    @Path("clock")
    public Response setClockAsCurrentSong() {
        if (!"clock".equals(song.getTitle())) {
            if (nextSong == null) {
                nextSong = song;
            }
            song = new Song(messages.getString("clock"), Song.Type.CLOCK);
            SongWebSocket.songId = song.hashCode();
            SongWebSocket.songType = song.getType();
            SongWebSocket.notifyDataChanged();
        }
        return Response.ok().build();
    }

    @POST
    @Path("next")
    public Response setNextSongAsCurrentSong() {
        if (nextSong != null && nextSong.getFileName() != null
                && !nextSong.getFileName().equals(song.getFileName())) {
            if (song.getType() == Song.Type.CLOCK) {
                // do not change the page number, because we return from the clock
                song = nextSong;
                nextSong = null;
                SongWebSocket.songId = song.hashCode();
                SongWebSocket.songType = song.getType();
                SongWebSocket.notifyDataChanged();
            } else {
                setSong(nextSong);
            }
        }
        return Response.ok().build();
    }

    @POST
    @Path("lang/{newLang}")
    public Response setLangForCurrentSong(@PathParam("newLang") int newLang) {
        if (newLang <= 0 || newLang > song.getLangCount()) {
            return Response.status(400).build();
        }
        songParser.setLangForSong(song.getFileName(), newLang);
        setSong(song.getFileName());
        return Response.ok().build();
    }

    @POST
    @Path("chords/{enable}")
    public Response setChordsEnabled(@PathParam("enable") boolean enable) {
        GlobalPreferences.setShowChords(enable);
        if (song != null && song.getFileName() != null) {
            setSong(song.getFileName());
        }
        return Response.ok().build();
    }

    public int getPage() {
        return SongWebSocket.page;
    }

    public Song getSong() {
        return song;
    }

    int getClientCount() {
        return SongWebSocket.sessions.size();
    }

    @ServerEndpoint("/song/ws")
    public static class SongWebSocket {
        private static final Logger logger = LoggerFactory.getLogger(SongWebSocket.class);
        private static final List<Session> sessions = Collections.synchronizedList(new ArrayList<>());
        private static final List<Session> statusSessions = Collections.synchronizedList(new ArrayList<>());
        private static int songId;
        private static Song.Type songType;
        private static int page;

        static void notifyDataChanged() {
            int clientsCount = sessions.size();
            String format = "{\"songId\": %d, \"songType\": \"%s\", \"page\": %d, \"clients\": %d}";
            String data = String.format(format, songId, songType, page, clientsCount);
            logger.debug("send data to clients");
            for (Session session : sessions) {
                session.getAsyncRemote().sendText(data);
            }
            for (Session session : statusSessions) {
                session.getAsyncRemote().sendText(data);
            }
        }

        @OnOpen
        public void onOpen(Session session) {
            logger.debug("session open ");
            session.setMaxIdleTimeout(Long.MAX_VALUE);
            String query = session.getQueryString();
            if (query != null && query.replace(" ", "").contains("status=true")) {
                statusSessions.add(session);
            } else {
                sessions.add(session);
                notifyDataChanged(); // new song client -> update
            }
        }

        @OnClose
        public void onClose(Session session, CloseReason closeReason) {
            if (sessions.remove(session)) {
                logger.debug("session close ", closeReason);
                notifyDataChanged();
            }
            statusSessions.remove(session);
        }

        @OnError
        public void onError(Session session, Throwable thr) {
            logger.error("session error", thr);
        }
    }
}