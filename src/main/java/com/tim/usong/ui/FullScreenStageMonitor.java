package com.tim.usong.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.prefs.Preferences;

public class FullScreenStageMonitor extends JWindow {
    private static final Object lock = new Object();
    private static FullScreenStageMonitor INSTANCE;
    private final Preferences prefs = Preferences.userNodeForPackage(FullScreenStageMonitor.class)
            .node("fullscreenStageMonitor");
    private final Thread shutdownHook = new Thread(this::savePrefs);
    private final WebJFXPanel webPanel;

    public static void showOnDisplay(int displayIndex) throws IllegalArgumentException {
        GraphicsDevice[] screenDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        if (displayIndex >= screenDevices.length) {
            throw new IllegalArgumentException("Display not found");
        }
        synchronized (lock) {
            // Allow only one instance at a time
            if (INSTANCE != null) {
                INSTANCE.dispose();
            }
            INSTANCE = new FullScreenStageMonitor();
            screenDevices[displayIndex].setFullScreenWindow(INSTANCE);
        }
    }

    public static boolean isDisplaying() {
        FullScreenStageMonitor instance = INSTANCE;
        return instance != null && instance.isVisible();
    }

    public static void close() {
        synchronized (lock) {
            if (INSTANCE != null) {
                INSTANCE.dispose();
                INSTANCE = null;
            }
        }
    }

    private FullScreenStageMonitor() {
        setBackground(Color.BLACK);
        String url = "http://localhost/song";
        webPanel = new WebJFXPanel(prefs.getDouble("zoom", 1), url, FullScreenStageMonitor::close);
        add(webPanel);
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        // JFX tends to freeze if mouse is dragged
        // But only inside a JWindow (not JFrame)
        // And only on jre newer than 8
        // So what to do? Just Capture all Mouse inputs and dispatch only right clicks to the jfx view
        // Big Hack but it works.
        getGlassPane().setVisible(true);
        getGlassPane().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    webPanel.dispatchEvent(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    webPanel.dispatchEvent(e);
                }
            }
        });
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
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
    }
}