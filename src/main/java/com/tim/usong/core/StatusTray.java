package com.tim.usong.core;

import com.google.common.base.Strings;
import com.tim.usong.USongApplication;
import com.tim.usong.core.entity.Song;
import com.tim.usong.resource.SongResource;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;

public class StatusTray implements Managed {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final USongApplication app;
    private final USongApplication.SongBeamerSettings songBeamerSettings;
    private final SongbeamerListener songbeamerListener;
    private final SongParser songParser;
    private final SongResource songResource;
    private final SystemTray systemTray;
    private final TrayIcon trayIcon;
    private Process uSongControlProcess;

    public StatusTray(USongApplication app, USongApplication.SongBeamerSettings songBeamerSettings,
                      SongbeamerListener songbeamerListener, SongParser songParser,
                      SongResource songResource) {
        this.app = app;
        this.songBeamerSettings = songBeamerSettings;
        this.songbeamerListener = songbeamerListener;
        this.songParser = songParser;
        this.songResource = songResource;
        systemTray = SystemTray.getSystemTray();

        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icon-small2.png"));
        trayIcon = new TrayIcon(image, USongApplication.APP_NAME, new PopupMenu());
        trayIcon.setImageAutoSize(true);
    }

    @Override
    public void start() {
        PopupMenu popupMenu = trayIcon.getPopupMenu();

        popupMenu.add("Status");
        CheckboxMenuItem previewCheckBox = new CheckboxMenuItem("Vorschau", false);
        previewCheckBox.addItemListener(this::onPreviewCheckboxStateChange);
        popupMenu.add(previewCheckBox);
        popupMenu.addSeparator();
        String hostname = getHostname(null);
        if (hostname != null) popupMenu.add("http://" + hostname);
        popupMenu.addSeparator();
        popupMenu.add("Beenden");

        popupMenu.addActionListener(e -> {
            String actionComand = e.getActionCommand();
            if (actionComand.equals("Status")) {
                showStatusWindow();
            } else if (actionComand.startsWith("http://")) {
                openBrowser(actionComand + "/song?admin=true");
            } else if (actionComand.equals("Beenden")) {
                if (uSongControlProcess != null) uSongControlProcess.destroy();
                songResource.shutDown();
                app.shutdown();
            }
        });

        trayIcon.addActionListener(e -> showStatusWindow());
        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            logger.error("Failed to add tray icon to system tray", e);
        }
    }

    @Override
    public void stop() {
        systemTray.remove(trayIcon);
    }

    private void onPreviewCheckboxStateChange(ItemEvent event) {
        if (event.getStateChange() == ItemEvent.SELECTED) {
            try {
                String path = USongApplication.LOCAL_DIR + "uSongControl.jar";
                File workingDir = new File(System.getProperty("java.home"));
                uSongControlProcess = Runtime.getRuntime().exec("java -jar " + path, null, workingDir);
            } catch (IOException e) {
                logger.error("Failed to open preview window", e);
                USongApplication.showErrorDialog("Fehler beim Öffnen des Vorschau Fensters\n" + e, true);
                ((CheckboxMenuItem) event.getItem()).setState(false);
            }
        } else if (uSongControlProcess != null) {
            uSongControlProcess.destroy();
        }
    }

    private void showStatusWindow() {
        Song song = songResource.getSong();
        int page = songResource.getPage();
        String sbVersion = songBeamerSettings.version;
        if (Strings.isNullOrEmpty(sbVersion)) sbVersion = "unbekannt";

        StringBuilder messageBuilder = new StringBuilder()
                .append("Hostname: \t\t").append(getHostname("unbekannt"))
                .append("\nIP Addresse: \t\t").append(getHostAddress())
                .append("\nSongBeamer Sender: \t")
                .append(songbeamerListener.isConnected() ? "Verbunden" : "Nicht verbunden")
                .append("\nAnzahl aktiver Clients: \t").append(songResource.getClientsCount())
                .append("\n\nOrdner für Songs: \t").append(Strings.nullToEmpty(songParser.getSongDir())).append("  ")
                .append("\nAnzahl Songs: \t").append(countSongs(songParser.getSongDir()))
                .append("\n\nAktueller Song: \t").append(song.getTitle())
                .append("\nAktuelle Foliennummer: \t").append(page == -1 ? "-" : page + 1);
        if (song.getLangCount() > 1) {
            int currentLang = songParser.getLangForSong(song.getTitle());
            messageBuilder.append("\nAktuelle Sprache: \t").append(currentLang);
        }
        messageBuilder.append("\n\nSongBeamer Version: \t").append(sbVersion)
                .append("\nVersion: \t\t").append(USongApplication.APP_VERSION);


        ImageIcon logoIcon = new ImageIcon(StatusTray.class.getResource("/icon-small.png"));
        JOptionPane.showMessageDialog(null, new JTextArea(messageBuilder.toString()),
                USongApplication.APP_NAME, JOptionPane.PLAIN_MESSAGE, logoIcon);
    }

    private long countSongs(String songPath) {
        String[] files = new File(songPath).list();
        if (files == null) return -1;
        return Arrays.stream(files).filter(s -> s.endsWith(".sng")).count();
    }

    private String getHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "unbekannt";
        }
    }

    private String getHostname(String defaultValue) {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return defaultValue;
        }
    }

    private void openBrowser(String url) {
        try {
            Desktop.getDesktop().browse(new URL(url).toURI());
        } catch (Exception ignore) {
        }
    }
}
