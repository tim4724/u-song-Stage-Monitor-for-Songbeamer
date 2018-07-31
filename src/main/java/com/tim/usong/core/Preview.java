package com.tim.usong.core;

import io.dropwizard.lifecycle.Managed;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

public class Preview implements Managed {
    private final Preferences prefs = Preferences.userNodeForPackage(getClass());
    private JFrame previewFrame;

    public void setVisible(boolean visible) {
        if (previewFrame != null) {
            previewFrame.dispose();
            previewFrame = null;
        }
        if (visible) {
            previewFrame = new PreviewFrame();
        }
    }

    public boolean isVisible() {
        return previewFrame != null && previewFrame.isVisible();
    }

    @Override
    public void start() {
        boolean visible = prefs.getBoolean("showPreview", true);
        setVisible(visible);
    }

    @Override
    public void stop() {
        prefs.putBoolean("showPreview", isVisible());
        setVisible(false);
    }

    private static class PreviewFrame extends JFrame {
        private final Preferences prefs = Preferences.userNodeForPackage(getClass());
        private final KeyCombination increaseZoom = new KeyCodeCombination(KeyCode.PLUS, KeyCombination.CONTROL_DOWN);
        private final KeyCombination increaseZoom2 = new KeyCodeCombination(KeyCode.ADD, KeyCombination.CONTROL_DOWN);
        private final KeyCombination decreseZoom = new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN);
        private final KeyCombination decreseZoom2 = new KeyCodeCombination(KeyCode.SUBTRACT, KeyCombination.CONTROL_DOWN);
        private double zoom;

        PreviewFrame() {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            setTitle("Stage Monitor Vorschau");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setType(Type.NORMAL);
            setAlwaysOnTop(true);
            setIconImage(toolkit.getImage(getClass().getResource("/icon-small2.png")));

            GraphicsConfiguration config = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDefaultConfiguration();
            Rectangle screenBounds = config.getBounds();
            Insets screenInsets = toolkit.getScreenInsets(config);
            int height = prefs.getInt("height", 200);
            int width = prefs.getInt("width", 300);
            int x = prefs.getInt("x", 0);
            int y = prefs.getInt("y", screenBounds.height - height - screenInsets.bottom);
            setBounds(x, y, width, height);

            final JFXPanel jfxPanel = new JFXPanel();
            add(jfxPanel);
            setVisible(true);

            // avoid that some kinda important thread dies or something if preview window is closed
            Platform.setImplicitExit(false);

            Platform.runLater(() -> {
                final WebView webView = new WebView();
                webView.setZoom(zoom = prefs.getDouble("zoom", 0.2));
                WebEngine webEngine = webView.getEngine();
                jfxPanel.setScene(new Scene(webView));
                webEngine.load("http://localhost/song?admin=true");

                final ContextMenu contextMenu = new ContextMenu();
                javafx.scene.control.MenuItem plus = new javafx.scene.control.MenuItem("Zoom +");
                javafx.scene.control.MenuItem minus = new javafx.scene.control.MenuItem("Zoom -");
                javafx.scene.control.MenuItem reload = new javafx.scene.control.MenuItem("Reload");
                contextMenu.getItems().addAll(plus, minus, reload);

                plus.setOnAction(event -> webView.setZoom(zoom += 0.05));
                minus.setOnAction(event -> webView.setZoom(zoom -= 0.05));
                reload.setOnAction(event -> webEngine.reload());

                webView.setContextMenuEnabled(false);
                webView.setOnContextMenuRequested(event -> {
                    event.consume();
                    contextMenu.show(webView, event.getScreenX(), event.getScreenY());
                });
                webView.setOnKeyPressed(event -> {
                    if (increaseZoom.match(event) || increaseZoom2.match(event)) {
                        webView.setZoom(zoom += 0.05);
                    } else if (decreseZoom.match(event) || decreseZoom2.match(event)) {
                        webView.setZoom(zoom -= 0.05);
                    } else if (event.getCode() == KeyCode.F5) {
                        webEngine.reload();
                    }
                });
            });
        }

        @Override
        public void dispose() {
            prefs.putDouble("zoom", zoom);
            prefs.putInt("height", getHeight());
            prefs.putInt("width", getWidth());
            prefs.putInt("x", getX());
            prefs.putInt("y", getY());
            super.dispose();
        }
    }
}
