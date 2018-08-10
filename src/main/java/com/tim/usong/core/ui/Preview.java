package com.tim.usong.core.ui;

import com.tim.usong.USongApplication;
import io.dropwizard.lifecycle.Managed;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class Preview implements Managed {
    private final Preferences prefs = Preferences.userNodeForPackage(Preview.class);
    private PreviewFrame previewFrame;

    public void setVisible(boolean visible) {
        if (isVisible()) {
            previewFrame.dispose(); // free resources
            previewFrame = null;  // free all resources
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
        boolean visible = prefs.getBoolean("show_preview", true);
        setVisible(visible);
    }

    @Override
    public void stop() {
        // System.exit(0) -> stop() -> isVisible() will still be true even if window is not visible (for some reason)
        boolean visible = isVisible();
        prefs.putBoolean("show_preview", visible);
        // do no dispose previewFrame, as this will end in a deadlock
        // just manually store the preferences, because dispose() may not have been called on the preview window
        if (previewFrame != null && visible) {
            previewFrame.savePrefs();
        }
    }

    private static class PreviewFrame extends JFrame {
        private final Logger logger = LoggerFactory.getLogger(PreviewFrame.class);
        private final ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
        private final Preferences prefs = Preferences.userNodeForPackage(PreviewFrame.class);
        private final KeyCombination increaseZoom = new KeyCodeCombination(KeyCode.PLUS, KeyCombination.CONTROL_DOWN);
        private final KeyCombination increaseZoom2 = new KeyCodeCombination(KeyCode.ADD, KeyCombination.CONTROL_DOWN);
        private final KeyCombination decreseZoom2 = new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN);
        private final KeyCombination decreseZoom = new KeyCodeCombination(KeyCode.SUBTRACT, KeyCombination.CONTROL_DOWN);
        private final String url = "http://localhost/song?admin=true";
        private WebView webView;
        private double zoom;

        PreviewFrame() {
            ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            setTitle(messages.getString("previewTitle"));
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setType(Type.POPUP); // TYPE POPUP will not make the taskbar icon blink because of new window
            setAlwaysOnTop(true);
            setIconImage(toolkit.getImage(getClass().getResource("/icon-small2.png")));

            GraphicsConfiguration config = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDefaultConfiguration();
            Rectangle screenBounds = config.getBounds();
            Insets screenInsets = toolkit.getScreenInsets(config);
            int width = prefs.getInt("width", 300);
            int height = prefs.getInt("height", 200);
            int x = prefs.getInt("x", 0);
            int y = prefs.getInt("y", screenBounds.height - height - screenInsets.bottom);
            setBounds(x, y, width, height);

            final JFXPanel jfxPanel = new JFXPanel();
            add(jfxPanel);
            setVisible(true);

            // avoid that some kinda important jfx thread dies or something if preview window is closed
            Platform.setImplicitExit(false);

            Platform.runLater(() -> {
                webView = new WebView();
                webView.setZoom(zoom = prefs.getDouble("zoom", 0.2));
                WebEngine webEngine = webView.getEngine();
                jfxPanel.setScene(new Scene(webView));
                webEngine.load(url);

                ContextMenu contextMenu = new ContextMenu();
                MenuItem plus = new MenuItem(messages.getString("zoomIncrease"));
                MenuItem minus = new MenuItem(messages.getString("zoomDecrease"));
                MenuItem reload = new MenuItem(messages.getString("reload"));
                MenuItem openInBrowser = new MenuItem(messages.getString("openInBrowser"));
                contextMenu.getItems().addAll(plus, minus, reload, openInBrowser);

                plus.setOnAction(e -> webView.setZoom(zoom += 0.05));
                minus.setOnAction(e -> webView.setZoom(zoom -= 0.05));
                reload.setOnAction(e -> webEngine.reload());
                openInBrowser.setOnAction(e -> openBrowser());

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
            // stop webview engine
            Platform.runLater(() -> webView.getEngine().load(null));
            savePrefs();
            super.dispose();
        }

        void savePrefs() {
            prefs.putDouble("zoom", zoom);
            prefs.putInt("height", getHeight());
            prefs.putInt("width", getWidth());
            prefs.putInt("x", getX());
            prefs.putInt("y", getY());
        }

        private void openBrowser() {
            try {
                Desktop.getDesktop().browse(new URL(url).toURI());
            } catch (Exception e) {
                logger.error("Failed to open browser", e);
                USongApplication.showErrorDialogAsync(messages.getString("browserOpenError"), e);
            }
        }
    }
}
