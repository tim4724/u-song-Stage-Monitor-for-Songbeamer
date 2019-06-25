package com.tim.usong.ui;

import com.tim.usong.GlobalPreferences;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

public class FullScreenStageMonitor extends JWindow {
    private static FullScreenStageMonitor INSTANCE;
    private final Preferences prefs = Preferences.userNodeForPackage(FullScreenStageMonitor.class)
            .node("fullscreenStageMonitor");
    private final WebJFXPanel webPanel;

    public static void showOnDisplay(int displayIndex) throws IllegalArgumentException {
        GraphicsDevice[] screenDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        if (displayIndex >= screenDevices.length) {
            throw new IllegalArgumentException("Display not found");
        }
        if (INSTANCE != null) {
            INSTANCE.dispose();
        }
        // Allow only one instance at a time
        INSTANCE = new FullScreenStageMonitor(screenDevices[displayIndex]);
        INSTANCE.setVisible(true);
    }

    public static boolean isDisplaying() {
        return INSTANCE != null && INSTANCE.isVisible();
    }

    public static void close() {
        if (INSTANCE != null) {
            INSTANCE.dispose();
            INSTANCE = null;
        }
        GlobalPreferences.setFullscreenDisplay(-1);
    }

    private FullScreenStageMonitor(GraphicsDevice device) {
        setBackground(Color.BLACK);
        Rectangle bounds = device.getDefaultConfiguration().getBounds();
        setSize(bounds.width, bounds.height);
        setLocation(bounds.x, bounds.y);
        String url = "http://localhost/song";
        webPanel = new WebJFXPanel(prefs.getDouble("zoom", 1), url, FullScreenStageMonitor::close);
        add(webPanel);
        Runtime.getRuntime().addShutdownHook(new Thread(this::savePrefs));
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            webPanel.reload();
        } else {
            webPanel.stopWebEngine();
        }
    }

    @Override
    public void dispose() {
        savePrefs();
        webPanel.stopWebEngine();
        super.dispose();
    }

    private void savePrefs() {
        prefs.putDouble("zoom", webPanel.getZoom());
    }
}