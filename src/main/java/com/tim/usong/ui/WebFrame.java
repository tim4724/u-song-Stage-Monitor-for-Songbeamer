package com.tim.usong.ui;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

public class WebFrame extends JFrame {
    private static final Map<String, WebFrame> activeFrames = new HashMap<>();
    private final Preferences prefs;
    private final String url;
    private final WebJFXPanel webPanel;

    WebFrame(String title, String url, Preferences prefs, int width, int height, double zoom) {
        this.url = url;
        this.prefs = prefs;
        setTitle(title);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icon-small2.png")));

        width = prefs.getInt("width", width);
        height = prefs.getInt("height", height);
        setBackground(Color.BLACK);
        setSize(width, height);

        // set position after settings size!
        int x = prefs.getInt("x", -1);
        int y = prefs.getInt("y", -1);
        if (x != -1 || y != -1) {
            setLocation(x, y);
        } else {
            setLocationRelativeTo(null);
        }

        webPanel = new WebJFXPanel(prefs.getDouble("zoom", zoom), url, this::dispose);
        add(webPanel);
        Runtime.getRuntime().addShutdownHook(new Thread(this::savePrefs));
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            WebFrame oldWebFrame = activeFrames.get(url);
            if (oldWebFrame != null && oldWebFrame.isVisible()) {
                oldWebFrame.dispose();
            }
            activeFrames.put(url, this);
            webPanel.reload();

        } else {
            webPanel.stopWebEngine();
        }
    }

    @Override
    public void dispose() {
        // stop webview engine
        webPanel.stopWebEngine();
        savePrefs();
        super.dispose();
    }

    private void savePrefs() {
        prefs.putDouble("zoom", webPanel.getZoom());
        prefs.putInt("height", getHeight());
        prefs.putInt("width", getWidth());
        prefs.putInt("x", getX());
        prefs.putInt("y", getY());
    }
}