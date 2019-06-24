package com.tim.usong.ui;

import com.tim.usong.GlobalPreferences;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class FullScreenStageMonitor extends JWindow {
    private static FullScreenStageMonitor INSTANCE;
    private final ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
    private final Preferences prefs = Preferences.userNodeForPackage(FullScreenStageMonitor.class)
            .node("fullscreenStageMonitor");
    private final KeyCombination increaseZoom = new KeyCodeCombination(KeyCode.PLUS, KeyCombination.CONTROL_DOWN);
    private final KeyCombination increaseZoom2 = new KeyCodeCombination(KeyCode.ADD, KeyCombination.CONTROL_DOWN);
    private final KeyCombination decreaseZoom = new KeyCodeCombination(KeyCode.SUBTRACT, KeyCombination.CONTROL_DOWN);
    private final KeyCombination decreaseZoom2 = new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN);
    private WebView webView;
    private double zoom = 1;

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

    public static void remove() {
        if (INSTANCE != null) {
            INSTANCE.dispose();
        }
        GlobalPreferences.setFullscreenDisplay(-1);
    }

    private FullScreenStageMonitor(GraphicsDevice device) {
        setAlwaysOnTop(true);
        setBackground(Color.BLACK);
        Rectangle bounds = device.getDefaultConfiguration().getBounds();
        setSize(bounds.width, bounds.height);
        setLocation(bounds.x, bounds.y);

        final JFXPanel jfxPanel = new JFXPanel();
        jfxPanel.setBackground(Color.BLACK);
        add(jfxPanel);

        // avoid that some kinda important jfx thread dies or something if preview window is closed
        Platform.setImplicitExit(false);

        Platform.runLater(() -> {
            webView = new WebView();
            webView.setZoom(zoom = prefs.getDouble("zoom", zoom));
            WebEngine webEngine = webView.getEngine();
            webEngine.load("http://localhost/song");
            jfxPanel.setScene(new Scene(webView));

            MenuItem plus = new MenuItem(messages.getString("zoomIncrease"));
            MenuItem minus = new MenuItem(messages.getString("zoomDecrease"));
            MenuItem reload = new MenuItem(messages.getString("reload"));
            MenuItem close = new MenuItem(messages.getString("exit"));
            plus.setOnAction(e -> webView.setZoom(zoom += 0.02));
            minus.setOnAction(e -> webView.setZoom(zoom -= 0.02));
            reload.setOnAction(e -> webEngine.reload());
            close.setOnAction(e -> remove());

            ContextMenu contextMenu = new ContextMenu();
            contextMenu.getItems().addAll(plus, minus, reload, close);

            webView.setContextMenuEnabled(false);
            webView.setOnContextMenuRequested(event -> {
                event.consume();
                contextMenu.show(webView, event.getScreenX(), event.getScreenY());
            });
            webView.setOnKeyPressed(event -> {
                if (increaseZoom.match(event) || increaseZoom2.match(event)) {
                    webView.setZoom(zoom += 0.02);
                } else if (decreaseZoom.match(event) || decreaseZoom2.match(event)) {
                    webView.setZoom(zoom -= 0.02);
                } else if (event.getCode() == KeyCode.F5) {
                    webEngine.reload();
                }
            });
        });
        Runtime.getRuntime().addShutdownHook(new Thread(this::savePrefs));
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            Platform.runLater(() -> webView.getEngine().load("http://localhost/song"));
        }
    }

    @Override
    public void dispose() {
        // stop webview engine
        Platform.runLater(() -> webView.getEngine().load(null));
        savePrefs();
        super.dispose();
    }

    private void savePrefs() {
        prefs.putDouble("zoom", zoom);
    }
}