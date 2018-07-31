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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
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
    private final Preview preview;
    private final SystemTray systemTray;
    private final TrayIcon trayIcon;

    public StatusTray(USongApplication app,
                      USongApplication.SongBeamerSettings songBeamerSettings,
                      SongbeamerListener songbeamerListener,
                      SongParser songParser,
                      SongResource songResource,
                      Preview preview) {
        this.app = app;
        this.songBeamerSettings = songBeamerSettings;
        this.songbeamerListener = songbeamerListener;
        this.songParser = songParser;
        this.songResource = songResource;
        this.preview = preview;
        systemTray = SystemTray.getSystemTray();

        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icon-small2.png"));
        trayIcon = new TrayIcon(image, USongApplication.APP_NAME, new PopupMenu());
        trayIcon.setImageAutoSize(true);
    }

    @Override
    public void start() {
        PopupMenu popupMenu = trayIcon.getPopupMenu();
        popupMenu.add("Status");
        CheckboxMenuItem previewCheckBox = new CheckboxMenuItem("Vorschau", preview.isVisible());
        previewCheckBox.addItemListener(event -> preview.setVisible(event.getStateChange() == ItemEvent.SELECTED));
        popupMenu.add(previewCheckBox);
        popupMenu.addSeparator();
        String hostname = getHostname(null);
        popupMenu.add(hostname != null ? "http://" + hostname : "http://localhost");
        popupMenu.addSeparator();
        popupMenu.add("Beenden");

        popupMenu.addActionListener(e -> {
            String actionComand = e.getActionCommand();
            if (actionComand.equals("Status")) {
                showStatusWindow();
            } else if (actionComand.startsWith("http://")) {
                openBrowser(actionComand + "/song?admin=true");
            } else if (actionComand.equals("Beenden")) {
                songResource.shutDown();
                app.shutdown();
            }
        });

        trayIcon.addActionListener(e -> showStatusWindow());
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                // update the checkbox state
                previewCheckBox.setState(preview.isVisible());
            }
        });
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

    private void showStatusWindow() {
        Song song = songResource.getSong();
        int page = songResource.getPage();
        String sbVersion = songBeamerSettings.version;
        if (Strings.isNullOrEmpty(sbVersion)) sbVersion = "Unbekannt";

        StringBuilder messageBuilder = new StringBuilder()
                .append("Hostname: \t\t").append(getHostname("Unbekannt"))
                .append("\nIP Addresse: \t\t").append(getHostAddress())
                .append("\nSongBeamer Sender: \t")
                .append(songbeamerListener.isConnected() ? "Verbunden" : "Nicht verbunden")
                .append("\nAnzahl aktiver Clients: \t").append(songResource.getClientsCount())
                .append("\n\nOrdner für Songs: \t").append(Strings.nullToEmpty(songParser.getSongDir())).append("  ")
                .append("\nAnzahl Songs: \t\t").append(countSongs(songParser.getSongDir()))
                .append("\n\nAktueller Song: \t").append(song.getTitle())
                .append("\nAktuelle Foliennummer: \t").append(page == -1 ? "-" : page + 1);
        if (song.getLangCount() > 1) {
            int currentLang = songParser.getLangForSong(song.getTitle());
            messageBuilder.append("\nAktuelle Sprache: \t").append(currentLang);
        }
        messageBuilder.append("\n\nSongBeamer Version: \t").append(sbVersion)
                .append("\nVersion: \t\t").append(USongApplication.APP_VERSION);

        JTextArea textArea = new JTextArea(messageBuilder.toString());
        textArea.setEditable(false);
        ImageIcon logoIcon = new ImageIcon(StatusTray.class.getResource("/icon-small.png"));
        JOptionPane.showMessageDialog(null, textArea, USongApplication.APP_NAME,
                JOptionPane.PLAIN_MESSAGE, logoIcon);
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
            return "Unbekannt";
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
        } catch (Exception e) {
            logger.error("Failed to open browser", e);
            USongApplication.showErrorDialog("Fehler beim Öffnen des Browsers", true);
        }
    }
}
