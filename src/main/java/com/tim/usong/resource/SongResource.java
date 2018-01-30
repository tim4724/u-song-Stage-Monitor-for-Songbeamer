package com.tim.usong.resource;

import com.tim.usong.core.SongParser;
import com.tim.usong.core.entity.Song;
import com.tim.usong.view.SongView;
import io.dropwizard.jersey.sessions.Session;

import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Path("song")
public class SongResource {
    private final Object longPollingLock = new Object();
    private final Set<String> activeClients = Collections.synchronizedSet(new HashSet<>());
    private final SongParser songParser;

    private Song song;
    private int songId;
    private int page;

    public SongResource(SongParser songParser) {
        this.songParser = songParser;
        this.song = Song.waitForSongbeamer;
        this.songId = song.hashCode();
        this.page = -1;
    }

    public void setSongAndPage(String songFileName, int page) {
        setSongAndPage(songParser.parse(songFileName), page);
    }

    public void setPage(int page) {
        setSongAndPage(song, page);
    }

    public void setSongAndPage(Song song, int page) {
        if (!this.song.equals(song) || this.page != page) {
            this.song = song;
            this.page = page;
            this.songId = song.hashCode();
            activeClients.clear();
            synchronized (longPollingLock) {
                longPollingLock.notifyAll();
            }
        }
    }

    public void shutDown() {
        synchronized (longPollingLock) {
            longPollingLock.notifyAll();
        }
        synchronized (activeClients) {
            activeClients.notifyAll();
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public SongView getSong(@Session HttpSession session, @QueryParam("admin") boolean admin) {
        session.setAttribute("clientCount", 0);
        session.setAttribute("song", songId);
        session.setAttribute("page", -2);
        return new SongView(song, admin);
    }

    @GET
    @Path("page")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getCurrentPage(@Session HttpSession session) throws InterruptedException {
        if (!activeClients.contains(session.getId())) {
            synchronized (activeClients) {
                activeClients.add(session.getId());
                activeClients.notifyAll();
            }
        }

        Integer songId = (Integer) session.getAttribute("song");
        Integer page = (Integer) session.getAttribute("page");
        synchronized (longPollingLock) {
            if (Objects.equals(songId, this.songId) && Objects.equals(page, this.page)) {
                longPollingLock.wait(60000);//no changes yet, therefore wait;
            }
        }

        if (!Objects.equals(songId, this.songId)) {
            return Response.status(205).build();//song has changed; HTTP 205: Reset Content
        }
        if (!Objects.equals(page, this.page)) {
            session.setAttribute("page", this.page);
            return Response.ok(this.page).build();
        }
        return Response.status(304).build();
    }

    @POST
    @Path("page/{newPage}")
    public Response setPageForCurrentSong(@PathParam("newPage") Integer newPage) {
        int totalPages = song.getSections().stream().mapToInt(s -> s.getPages().size()).sum();
        if (newPage == null || newPage < 0 || newPage >= totalPages) {
            return Response.status(400).build();
        }
        setPage(newPage);
        return Response.ok().build();
    }

    @POST
    @Path("lang/{newLang}")
    public Response setLangForCurrentSong(@PathParam("newLang") Integer newLang) {
        if (newLang == null || newLang > song.getLangCount() || newLang <= 0) {
            return Response.status(400).build();
        }
        songParser.setLangForSong(song.getTitle(), newLang);
        Song newSong = songParser.parse(song.getFileName());
        setSongAndPage(newSong, page);
        return Response.ok().build();
    }

    @GET
    @Path("activeClients")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getActiveClients(@Session HttpSession session) throws InterruptedException {
        synchronized (activeClients) {
            if (Objects.equals(session.getAttribute("clientCount"), activeClients.size())) {
                activeClients.wait(60000);
            }
        }

        int activeClientsCount = activeClients.size();
        if (Objects.equals(session.getAttribute("clientCount"), activeClientsCount)) {
            return Response.status(304).build();
        }
        session.setAttribute("clientCount", activeClientsCount);
        return Response.ok(activeClientsCount).build();
    }

    public int getClientsCount() {
        return activeClients.size();
    }

    public int getPage() {
        return page;
    }

    public Song getSong() {
        return song;
    }
}