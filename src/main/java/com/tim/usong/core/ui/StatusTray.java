package com.tim.usong.core.ui;

import com.tim.usong.USongApplication;
import com.tim.usong.util.AutoStartUtil;
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

public class StatusTray implements Managed {
    private static final Logger logger = LoggerFactory.getLogger(StatusTray.class);
    private static final ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
    private PreviewFrame previewFrame;

    public StatusTray(PreviewFrame previewFrame) {
        this.previewFrame = previewFrame;
    }

    @Override
    public void start() {
        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icon-small2.png"));
        TrayIcon trayIcon = new TrayIcon(image, USongApplication.APP_NAME, new PopupMenu());
        trayIcon.setImageAutoSize(true);

        String statusMsg = messages.getString("status");
        String previewMsg = messages.getString("preview");
        String autoStartMsg = messages.getString("autostart");
        String exitMsg = messages.getString("exit");

        PopupMenu popupMenu = trayIcon.getPopupMenu();
        popupMenu.add(statusMsg);
        CheckboxMenuItem previewCheckBox = new CheckboxMenuItem(previewMsg, previewFrame.isVisible());
        previewCheckBox.addItemListener(event -> previewFrame.setVisible(event.getStateChange() == ItemEvent.SELECTED));
        popupMenu.add(previewCheckBox);

        CheckboxMenuItem autoStartCheckbox = new CheckboxMenuItem(autoStartMsg, AutoStartUtil.isAutostartEnabled());
        autoStartCheckbox.addItemListener(event -> setAutoStartEnabled(event.getStateChange() == ItemEvent.SELECTED));
        popupMenu.add(autoStartCheckbox);
        popupMenu.addSeparator();
        String hostname = getHostname();
        popupMenu.add(hostname != null ? "http://" + hostname : "http://localhost");
        popupMenu.addSeparator();
        popupMenu.add(exitMsg);

        popupMenu.addActionListener(e -> {
            String actionComand = e.getActionCommand();
            if (actionComand.equals(statusMsg)) {
                openStatusWindow();
            } else if (actionComand.startsWith("http://")) {
                openBrowser(actionComand + "/song?admin=true");
            } else if (actionComand.equals(exitMsg)) {
                try {
                    System.exit(0);
                } catch (Exception e1) {
                    System.exit(-1);
                }
            }
        });

        trayIcon.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openStatusWindow();
            }
        });
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                // update the checkbox states
                previewCheckBox.setState(previewFrame.isVisible());
                autoStartCheckbox.setState(AutoStartUtil.isAutostartEnabled());
            }
        });
        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            logger.error("Failed to add tray icon to system tray", e);
        }
    }

    private void setAutoStartEnabled(boolean enable) {
        try {
            AutoStartUtil.setAutoStartEnabled(enable);
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
        webFrame.setType(Window.Type.UTILITY);
        webFrame.setVisible(true);
    }

    @Override
    public void stop() {
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    private void openBrowser(String url) {
        try {
            Desktop.getDesktop().browse(new URL(url).toURI());
        } catch (Exception e) {
            logger.error("Failed to open browser", e);
            USongApplication.showErrorDialogAsync(messages.getString("browserOpenError"), e);
        }
    }
}
