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
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

public class SongbeamerListener implements Managed, Runnable {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
    private final Song noSongSelected;
    private final SongResource songResource;
    private final ServerSocket serverSocket;
    private final DocumentBuilder builder;
    private final Thread currentThread;
    private Process songBeamerProcess;
    private boolean connected = false;

    public SongbeamerListener(SongResource songResource) throws ParserConfigurationException, IOException {
        this.songResource = songResource;
        noSongSelected = new Song(messages.getString("noSongSelected"), Song.Type.INFO);
        builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        serverSocket = new ServerSocket(19150);
        currentThread = new Thread(this);
    }

    @Override
    public void start() {
        currentThread.start();
    }

    @Override
    public void stop() throws IOException {
        currentThread.interrupt();
        serverSocket.close();
        logger.info("Killing SBRemoteSender");
        songBeamerProcess.destroy();
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                logger.info("Starting SBRemoteSender");
                String cmd = USongApplication.LOCAL_DIR + "SBRemoteSender.exe";
                songBeamerProcess = Runtime.getRuntime().exec(cmd);
            } catch (Exception e) {
                logger.error("Starting SBRemoteSender failed", e);
                songResource.setSongAndPage(new Song(messages.getString("startSBRemoteClientError"), e), 0);
                USongApplication.showErrorDialogAsync(messages.getString("startSBRemoteClientError"), e);
            }

            try (Socket socket = serverSocket.accept()) {
                connected = true;
                receive(socket);
                connected = false;
                logger.error("Conenction to SBRemoteSender lost");
                songResource.setSongAndPage(new Song(messages.getString("lostConnectionToSB"), Song.Type.ERROR), 0);
                USongApplication.showErrorDialogAsync(messages.getString("lostConnectionToSB"), null);
            } catch (Exception e) {
                connected = false;
                if (!Thread.currentThread().isInterrupted()) {
                    logger.error("Conenction to SBRemoteSender lost", e);
                    songResource.setSongAndPage(new Song(messages.getString("lostConnectionToSB"), e), 0);
                    USongApplication.showErrorDialogAsync(messages.getString("lostConnectionToSB"), e);
                }
            }
        }
    }

    private void receive(Socket socket) throws IOException {
        songResource.setSongAndPage(noSongSelected, -1);
        final String startTag = "<SongBeamerIPC>";
        final String endTag = "</SongBeamerIPC>";

        InputStream inputStream = socket.getInputStream();
        byte[] data = new byte[1024];
        String in = "";
        int len;
        while ((len = inputStream.read(data)) != -1) {
            in = in + new String(data, 0, len, StandardCharsets.UTF_8);
            logger.debug("Received data: \n" + in);
            while (in.contains(endTag)) {
                try {
                    String xml = in.substring(in.indexOf(startTag), in.indexOf(endTag)) + endTag;
                    //TODO: maybe not really necessary to parse this thing as xml
                    Document document = builder.parse(new InputSource(new StringReader(xml)));
                    Node actionNode = document.getElementsByTagName("Remote").item(0).getFirstChild();
                    newActionFromSongbeamer(actionNode);
                } catch (Exception e) {
                    logger.error("Failed to parse data from SongBeamerSender", e);
                    Song errorSong = new Song(messages.getString("handlingSongbeamerActionError"), e);
                    songResource.setSongAndPage(errorSong, 0);
                    USongApplication.showErrorDialogAsync(messages.getString("handlingSongbeamerActionError"), e);
                }
                in = in.substring(in.indexOf(endTag) + endTag.length());
            }
        }
    }

    private void newActionFromSongbeamer(Node actionNode) {
        String action = actionNode.getNodeName().trim();
        String value = actionNode.getAttributes().getNamedItem("Param1").getNodeValue().trim();
        logger.debug("Songbeamer action: " + action + ": " + value);

        switch (action) {
            case "SBAction_LoadItem":
                songResource.setSong(value);
                break;
            case "SBAction_Presenter_Black":
            case "SBAction_Presenter_BGOnly":
                // Currently no text on projector -> -1
                songResource.setPage(-1);
                break;
            case "SBAction_Presenter_SetPage":
                // index starts at 0
                int page = Integer.parseInt(value) - 1;
                songResource.setPage(page);
                break;
        }
    }

    public boolean isConnected() {
        return connected;
    }
}
