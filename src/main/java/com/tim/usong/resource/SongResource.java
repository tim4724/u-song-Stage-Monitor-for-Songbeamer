package com.tim.usong.resource;

import com.tim.usong.core.SongParser;
import com.tim.usong.core.entity.Song;
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
import java.util.*;

@Path("song")
public class SongResource {
    private final SongParser songParser;
    private Song song;

    public SongResource(SongParser songParser) {
        this.songParser = songParser;
        ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
        setSongAndPage(new Song(messages.getString("waitingForSongbeamer")), -1);
    }

    public void setSongAndPage(String songFileName, int page) {
        setSongAndPage(songParser.parse(songFileName), page);
    }

    public void setSongAndPage(Song song, int page) {
        this.song = song;
        SongWebSocket.songId = song.hashCode();
        SongWebSocket.page = page;
        SongWebSocket.notifyDataChanged();
    }

    public void setPage(int page) {
        SongWebSocket.page = page;
        SongWebSocket.notifyDataChanged();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public SongView getSong(@Context HttpServletRequest request, @QueryParam("admin") boolean admin) {
        return new SongView(song, request.getLocale(), admin);
    }

    @POST
    @Path("page/{newPage}")
    public Response setPageForCurrentSong(@PathParam("newPage") int newPage) {
        if (newPage < 0 || newPage >= song.getPageCount()) {
            return Response.status(400).build();
        }
        setPage(newPage);
        return Response.ok().build();
    }

    @POST
    @Path("lang/{newLang}")
    public Response setLangForCurrentSong(@PathParam("newLang") int newLang) {
        if (newLang <= 0 || newLang > song.getLangCount()) {
            return Response.status(400).build();
        }
        songParser.setLangForSong(song.getTitle(), newLang);
        setSongAndPage(song.getFileName(), SongWebSocket.page);
        return Response.ok().build();
    }

    public int getPage() {
        return SongWebSocket.page;
    }

    public Song getSong() {
        return song;
    }

    public int getClientCount() {
        return SongWebSocket.sessions.size();
    }

    @ServerEndpoint("/song/ws")
    public static class SongWebSocket {
        private static final Logger logger = LoggerFactory.getLogger(SongWebSocket.class);
        private static final List<Session> sessions = Collections.synchronizedList(new ArrayList<>());
        private static int songId;
        private static int page;

        static void notifyDataChanged() {
            int clientsCount = sessions.size();
            String data = String.format("{\"songId\": %d, \"page\": %d, \"clients\": %d}", songId, page, clientsCount);
            logger.debug("send data to clients");
            for (javax.websocket.Session session : sessions) {
                session.getAsyncRemote().sendText(data);
            }
        }

        @OnOpen
        public void onOpen(Session session) {
            logger.debug("session open ");
            session.setMaxIdleTimeout(Long.MAX_VALUE);
            sessions.add(session);
            notifyDataChanged();
        }

        @OnClose
        public void onClose(Session session, CloseReason closeReason) {
            sessions.remove(session);
            logger.debug("session close ", closeReason);
            notifyDataChanged();
        }

        @OnError
        public void onError(Session session, Throwable thr) {
            logger.error("session error", thr);
        }
    }
}