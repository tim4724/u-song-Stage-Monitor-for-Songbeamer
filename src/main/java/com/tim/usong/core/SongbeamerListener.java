package com.tim.usong.core;

import com.tim.usong.USongApplication;
import com.tim.usong.core.entity.Song;
import com.tim.usong.resource.SongResource;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;

public class SongbeamerListener implements Managed, Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());
    private final SongResource songResource;
    private final ServerSocket serverSocket;
    private final DocumentBuilder builder;
    private final Thread currentThread;
    private String currentSongFilename = null;
    private String currentSongDisplayedFilename = null;
    private int currentPage = -1;
    private boolean connected = false;

    public SongbeamerListener(SongResource songResource) throws ParserConfigurationException, IOException {
        this.songResource = songResource;
        builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        serverSocket = new ServerSocket(19150);
        currentThread = new Thread(this);
    }

    @Override
    public void start() throws Exception {
        currentThread.start();
    }

    @Override
    public void stop() throws Exception {
        currentThread.interrupt();
        serverSocket.close();
        logger.info("Killing SBRemoteSender");
        Runtime.getRuntime().exec("taskkill /F /IM SBRemoteSender.exe");
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                logger.info("Starting SBRemoteSender");
                Runtime.getRuntime().exec("taskkill /F /IM SBRemoteSender.exe");
                new ProcessBuilder("SBRemoteSender.exe").start();
            } catch (Exception e) {
                logger.error(e.getMessage());
                songResource.setSongAndPage(new Song("Fehler beim starten von SB Remote Client", e), 0);
                USongApplication.showErrorDialogAsync("Fehler beim starten von SB Remote Client\n" + e, true);
            }

            try (Socket socket = serverSocket.accept()) {
                connected = true;
                receive(socket);
            } catch (Exception e) {
                if (!Thread.currentThread().isInterrupted()) {
                    connected = false;
                    logger.error(e.getMessage());
                    songResource.setSongAndPage(new Song("Verbindung zu Songbeamer unterbrochen", e), 0);
                    USongApplication.showErrorDialogAsync("Verbindung zu Songbeamer unterbrochen \n" + e, true);
                }
            }
        }
    }

    private void receive(Socket socket) throws IOException {
        songResource.setSongAndPage(Song.noSongSelected, 0);

        final String startTag = "<SongBeamerIPC>";
        final String endTag = "</SongBeamerIPC>";

        InputStream inputStream = socket.getInputStream();
        byte[] data = new byte[1024];
        String in = "";
        int len;
        while ((len = inputStream.read(data)) != -1) {
            in = in + new String(data, 0, len, "UTF-8");
            while (in.contains(endTag)) {
                logger.info("in: " + in);
                try {
                    String xml = in.substring(in.indexOf(startTag), in.indexOf(endTag)) + endTag;
                    Document document = builder.parse(new InputSource(new StringReader(xml)));
                    Node actionNode = document.getElementsByTagName("Remote").item(0).getFirstChild();
                    newActionFromSongbeamer(actionNode);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    songResource.setSongAndPage(new Song("Fehler beim parsen", e), 0);
                }
                in = in.substring(in.indexOf(endTag) + endTag.length());
            }
        }
    }

    private void newActionFromSongbeamer(Node actionNode) {
        String action = actionNode.getNodeName().trim();
        String value = actionNode.getAttributes().getNamedItem("Param1").getNodeValue().trim();
        logger.info("Songbeamer: " + action + ": " + value);

        switch (action) {
            case "SBAction_LoadItem":
                currentSongFilename = value;
                if (currentPage == -1 || currentSongFilename.endsWith(currentSongDisplayedFilename)) {
                    //Switch song if page is -1 or
                    //When a song was modified, currentSongFilename will be the absolute path (maybe a bug in SBRemoteSender).
                    //Reload song if currentSongFilename ends with currentSongDisplayedFilename
                    songResource.setSongAndPage(currentSongFilename, currentPage);
                }
                break;
            case "SBAction_Presenter_Black":
            case "SBAction_Presenter_BGOnly":
                //currently no text on beamer
                currentPage = -1;
                notifySongResource();
                break;
            case "SBAction_Presenter_SetPage":
                currentPage = Integer.parseInt(value) - 1;
                notifySongResource();
                break;
        }
    }

    private void notifySongResource() {
        if (currentSongFilename == null) {
            songResource.setSongAndPage(Song.noSongSelected, currentPage);
        } else if (!currentSongFilename.equals(currentSongDisplayedFilename)) {
            currentSongDisplayedFilename = currentSongFilename;
            songResource.setSongAndPage(currentSongFilename, currentPage);
        } else {
            songResource.setPage(currentPage);
        }
    }

    public boolean isConnected() {
        return connected;
    }
}
