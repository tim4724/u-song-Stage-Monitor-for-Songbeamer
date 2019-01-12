package com.tim.usong.ui;

import com.tim.usong.USongApplication;
import com.tim.usong.util.AutoStartUtil;
import com.tim.usong.util.NetworkHostUtils;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.*;
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
        String autoStartMsg = messages.getString("autostart");

        MenuItem statusItem = new MenuItem(messages.getString("status"));
        CheckboxMenuItem previewCheckBox = new CheckboxMenuItem(previewMsg, previewFrame.isVisible());
        CheckboxMenuItem autoStartCheckbox = new CheckboxMenuItem(autoStartMsg, AutoStartUtil.isAutostartEnabled());
        MenuItem hostItem = new MenuItem("http://" + getHostname());
        MenuItem ipAddressItem = new MenuItem("http://" + getIpAdress());
        MenuItem exitItem = new MenuItem(messages.getString("exit"));
        previewCheckBox.addItemListener(e -> previewFrame.setVisible(e.getStateChange() == ItemEvent.SELECTED));
        autoStartCheckbox.addItemListener(event -> setAutoStartEnabled(event.getStateChange() == ItemEvent.SELECTED));
        MenuItem tutorialItem = new MenuItem(messages.getString("tutorial"));
        MenuItem githubItem = new MenuItem(messages.getString("openGithub"));

        PopupMenu popupMenu = trayIcon.getPopupMenu();
        popupMenu.add(statusItem).addActionListener(e -> openStatusWindow());
        popupMenu.add(previewCheckBox);
        popupMenu.add(autoStartCheckbox);
        popupMenu.addSeparator();
        popupMenu.add(hostItem).addActionListener(e -> openBrowser(e.getActionCommand() + "/song?admin=true"));
        popupMenu.add(ipAddressItem).addActionListener(e -> openBrowser(e.getActionCommand() + "/song?admin=true"));
        popupMenu.addSeparator();
        popupMenu.add(tutorialItem).addActionListener(e -> new TutorialFrame().setVisible(true));
        popupMenu.add(githubItem).addActionListener(e -> openBrowser(GITHUB_LINK));
        popupMenu.add(exitItem).addActionListener(e -> System.exit(0));

        trayIcon.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openStatusWindow();
            }
        });
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                // update the checkbox states, which could have changed
                previewCheckBox.setState(previewFrame.isVisible());
                autoStartCheckbox.setState(AutoStartUtil.isAutostartEnabled());
                ipAddressItem.setLabel("http://" + getIpAdress());
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

    private void setAutoStartEnabled(boolean enable) {
        try {
            if (enable) {
                AutoStartUtil.enableAutoStart(USongApplication.getCurrentJarPath());
            } else {
                AutoStartUtil.disableAutoStart();
            }
        } catch (Exception e) {
            logger.error("Failed to edit registry", e);
            USongApplication.showErrorDialogAsync(messages.getString("autostartChangeFailed"), e);
        }
    }

    private void openStatusWindow() {
        String title = USongApplication.APP_NAME;
        String url = "http://localhost/status";
        Preferences prefs = Preferences.userNodeForPackage(WebFrame.class).node("status");
        WebFrame webFrame = new WebFrame(title, url, prefs, 800, 600, 0.6);
        webFrame.setVisible(true);
    }

    private void openBrowser(String url) {
        try {
            Desktop.getDesktop().browse(new URL(url).toURI());
        } catch (Exception e) {
            logger.error("Failed to open browser", e);
            USongApplication.showErrorDialogAsync(messages.getString("browserOpenError"), e);
        }
    }

    private String getHostname() {
        String hostname = NetworkHostUtils.getHostname();
        return hostname != null ? hostname : "localhost";
    }

    private String getIpAdress() {
        String ip = NetworkHostUtils.getHostAddress();
        return ip != null ? ip : "127.0.0.1";
    }
}
