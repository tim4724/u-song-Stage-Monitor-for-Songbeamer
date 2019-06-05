package com.tim.usong.ui;

import com.tim.usong.USongApplication;
import com.tim.usong.util.Browser;
import com.tim.usong.util.NetworkHost;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class UsongTray implements Managed {
    private static final Logger logger = LoggerFactory.getLogger(UsongTray.class);
    private static final ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
    private static final String GITHUB_LINK = "https://github.com/tim4724/u-song-Stage-Monitor-for-Songbeamer";
    private final PreviewFrame previewFrame;

    public UsongTray(PreviewFrame previewFrame) {
        this.previewFrame = previewFrame;
    }

    @Override
    public void start() {
        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icon-small2.png"));
        TrayIcon trayIcon = new TrayIcon(image, USongApplication.APP_NAME, new PopupMenu());
        trayIcon.setImageAutoSize(true);

        String previewMsg = messages.getString("preview");

        MenuItem statusItem = new MenuItem(messages.getString("status"));
        CheckboxMenuItem previewCheckBox = new CheckboxMenuItem(previewMsg, previewFrame.isVisible());
        MenuItem hostItem = new MenuItem("http://" + getHostname());
        MenuItem ipAddressItem = new MenuItem("http://" + getIpAddress());
        MenuItem exitItem = new MenuItem(messages.getString("exit"));
        previewCheckBox.addItemListener(e -> previewFrame.setVisible(e.getStateChange() == ItemEvent.SELECTED));
        MenuItem tutorialItem = new MenuItem(messages.getString("tutorial"));
        MenuItem settingsItem = new MenuItem(messages.getString("settings"));
        MenuItem githubItem = new MenuItem(messages.getString("openGithub"));

        PopupMenu popupMenu = trayIcon.getPopupMenu();
        popupMenu.add(statusItem).addActionListener(e -> openWebView("status"));
        popupMenu.add(previewCheckBox);
        popupMenu.add(hostItem).addActionListener(e -> Browser.open(e.getActionCommand() + "/song?admin=true"));
        popupMenu.add(ipAddressItem).addActionListener(e -> Browser.open(e.getActionCommand() + "/song?admin=true"));
        popupMenu.addSeparator();
        popupMenu.add(tutorialItem).addActionListener(e -> new TutorialFrame().setVisible(true));
        popupMenu.add(githubItem).addActionListener(e -> Browser.open(GITHUB_LINK));
        popupMenu.add(settingsItem).addActionListener(e -> openWebView("settings"));
        popupMenu.add(exitItem).addActionListener(e -> System.exit(0));

        trayIcon.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openWebView("settings");
            }
        });
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                // update the checkbox states, which could have changed
                previewCheckBox.setState(previewFrame.isVisible());
                ipAddressItem.setLabel("http://" + getIpAddress());
            }
        });
        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            logger.error("Failed to add tray icon to system tray", e);
        }
    }

    @Override
    public void stop() {
    }

    private void openWebView(String path) {
        String title = USongApplication.APP_NAME;
        String url = "http://localhost/" + path;
        Preferences prefs = Preferences.userNodeForPackage(WebFrame.class).node(path + "2");
        WebFrame webFrame = new WebFrame(title, url, prefs, 1000, 700, 0.8);
        webFrame.setVisible(true);
    }

    private String getHostname() {
        String hostname = NetworkHost.getHostname();
        return hostname != null ? hostname : "localhost";
    }

    private String getIpAddress() {
        String ip = NetworkHost.getHostAddress();
        return ip != null ? ip : "127.0.0.1";
    }
}
