package com.tim.usong.core;

import com.tim.usong.USongApplication;
import com.tim.usong.core.entity.Song;
import com.tim.usong.resource.SongResource;
import io.dropwizard.lifecycle.Managed;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;

public class StatusTray implements Managed {
    private final USongApplication app;
    private final USongApplication.SongBeamerSettings songBeamerSettings;
    private final SongbeamerListener songbeamerListener;
    private final SongParser songParser;
    private final SongResource songResource;
    private final SystemTray systemTray;
    private final TrayIcon trayIcon;

    public StatusTray(USongApplication app, USongApplication.SongBeamerSettings songBeamerSettings,
                      SongbeamerListener songbeamerListener, SongParser songParser,
                      SongResource songResource) {
        this.app = app;
        this.songBeamerSettings = songBeamerSettings;
        this.songbeamerListener = songbeamerListener;
        this.songParser = songParser;
        this.songResource = songResource;
        systemTray = SystemTray.getSystemTray();

        Image image = Toolkit.getDefaultToolkit().getImage(StatusTray.class.getResource("/icon-small2.png"));
        trayIcon = new TrayIcon(image, USongApplication.appName, new PopupMenu());
        trayIcon.setImageAutoSize(true);
    }

    @Override
    public void start() {
        PopupMenu popupMenu = trayIcon.getPopupMenu();
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                populatePopupMenu(popupMenu);
            }
        });
        popupMenu.addActionListener(e -> {
            String actionComand = e.getActionCommand();
            if (actionComand.equals("Zeige Status")) {
                showStatusWindow();
            } else if (actionComand.startsWith("http://")) {
                openBrowser(actionComand + "/song?admin=true");
            } else if (actionComand.equals("Beenden")) {
                songResource.shutDown();
                app.shutdown();
            }
        });
        trayIcon.addActionListener(e -> showStatusWindow());
        try {
            systemTray.add(trayIcon);
        } catch (AWTException ignore) {
        }
    }

    @Override
    public void stop() {
        systemTray.remove(trayIcon);
    }

    private void populatePopupMenu(PopupMenu popupMenu) {
        String hostname = getHostname(null);
        String hostAdress = getHostAddress(null);

        popupMenu.removeAll();
        popupMenu.add("Zeige Status");
        if (hostname != null) popupMenu.add("http://" + hostname);
        if (hostAdress != null) popupMenu.add("http://" + hostAdress);
        popupMenu.addSeparator();
        popupMenu.add("Beenden");
    }

    private void showStatusWindow() {
        Song song = songResource.getSong();
        int page = songResource.getPage();

        StringBuilder messageBuilder = new StringBuilder()
                .append("Hostname: \t\t").append(getHostname("unbekannt"))
                .append("\nIP Addresse: \t\t").append(getHostAddress("unbekannt"))
                .append("\nSongBeamer Sender: \t")
                .append(songbeamerListener.isConnected() ? "Verbunden" : "Nicht verbunden")
                .append("\nAnzahl aktiver Clients: \t").append(songResource.getClientsCount())
                .append("\n\nOrdner für Songs: \t").append(songParser.getSongDir()).append("    ")
                .append("\nAnzahl Songs: \t").append(countSongs(songParser.getSongDir()))
                .append("\n\nAktueller Song: \t").append(song.getTitle())
                .append("\nAktuelle Foliennummer: \t").append(page == -1 ? "-" : page + 1);
        if (song.getLangCount() > 1) {
            int currentLang = songParser.getLangForSong(song.getTitle());
            messageBuilder.append("\nAktuelle Sprache: \t").append(currentLang);
        }
        messageBuilder.append("\n\nSongBeamer Version: \t").append(songBeamerSettings.version)
                .append("\nVersion: \t\t").append(USongApplication.appVersion);
        JOptionPane.showMessageDialog(null, new JTextArea(messageBuilder.toString()), USongApplication.appName,
                JOptionPane.PLAIN_MESSAGE, new ImageIcon(StatusTray.class.getResource("/icon-small.png")));
    }

    private int countSongs(String songPath) {
        String[] files = new File(songPath).list();
        if (files == null) return -1;
        return (int) Arrays.stream(files).filter(s -> s.endsWith(".sng")).count();
    }

    private String getHostAddress(String defaultValue) {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return defaultValue;
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
