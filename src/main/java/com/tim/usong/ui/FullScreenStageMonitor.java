package com.tim.usong.ui;

import com.tim.usong.GlobalPreferences;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

        //TODO: ensure that multithreading will be no problem

        // Allow only one instance at a time
        INSTANCE = new FullScreenStageMonitor();
        screenDevices[displayIndex].setFullScreenWindow(INSTANCE);
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

    private FullScreenStageMonitor() {
        setBackground(Color.BLACK);
        String url = "http://localhost/song";
        webPanel = new WebJFXPanel(prefs.getDouble("zoom", 1), url, FullScreenStageMonitor::close);
        add(webPanel);

        // TODO: maybe not best idea, if window manually closed
        Runtime.getRuntime().addShutdownHook(new Thread(this::savePrefs));

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
    }
}