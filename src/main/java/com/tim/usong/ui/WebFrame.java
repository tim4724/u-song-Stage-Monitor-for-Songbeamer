package com.tim.usong.ui;

import com.tim.usong.USongApplication;
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

public class WebFrame extends JFrame {
    private final Logger logger = LoggerFactory.getLogger(WebFrame.class);
    private final ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
    private final KeyCombination increaseZoom = new KeyCodeCombination(KeyCode.PLUS, KeyCombination.CONTROL_DOWN);
    private final KeyCombination increaseZoom2 = new KeyCodeCombination(KeyCode.ADD, KeyCombination.CONTROL_DOWN);
    private final KeyCombination decreaseZoom = new KeyCodeCombination(KeyCode.SUBTRACT, KeyCombination.CONTROL_DOWN);
    private final KeyCombination decreaseZoom2 = new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN);
    private final Preferences prefs;
    private final String url;
    private WebView webView;
    private double zoom;

    WebFrame(String title, String url, Preferences prefs, int width, int height, double z) {
        this.zoom = z;
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

        final JFXPanel jfxPanel = new JFXPanel();
        jfxPanel.setBackground(Color.BLACK);
        add(jfxPanel);

        // avoid that some kinda important jfx thread dies or something if preview window is closed
        Platform.setImplicitExit(false);

        Platform.runLater(() -> {
            webView = new WebView();
            webView.setZoom(zoom = prefs.getDouble("zoom", zoom));
            WebEngine webEngine = webView.getEngine();
            webEngine.load(url);
            jfxPanel.setScene(new Scene(webView));

            MenuItem plus = new MenuItem(messages.getString("zoomIncrease"));
            MenuItem minus = new MenuItem(messages.getString("zoomDecrease"));
            MenuItem reload = new MenuItem(messages.getString("reload"));
            MenuItem openInBrowser = new MenuItem(messages.getString("openInBrowser"));
            plus.setOnAction(e -> webView.setZoom(zoom += 0.02));
            minus.setOnAction(e -> webView.setZoom(zoom -= 0.02));
            reload.setOnAction(e -> webEngine.reload());
            openInBrowser.setOnAction(e -> openBrowser());

            ContextMenu contextMenu = new ContextMenu();
            contextMenu.getItems().addAll(plus, minus, reload, openInBrowser);

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
            Platform.runLater(() -> webView.getEngine().load(url));
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