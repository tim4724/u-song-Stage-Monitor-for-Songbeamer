package com.tim.usong.core.ui;

import io.dropwizard.lifecycle.Managed;

import java.awt.*;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class PreviewFrame implements Managed {
    private WebFrame frame;
    private final Preferences prefs = Preferences
            .userNodeForPackage(PreviewFrame.class)
            .node("preview");

    public void setVisible(boolean visible) {
        if (isVisible()) {
            frame.dispose(); // free resources
            frame = null;  // free all resources
        }

        if (visible) {
            int x = prefs.getInt("x", -1);
            int y = prefs.getInt("y", -1);
            int defaultWidth = 300;
            int defaultHeight = 200;

            if (x == -1 && y == -1) {
                GraphicsConfiguration config = GraphicsEnvironment
                        .getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice()
                        .getDefaultConfiguration();
                Rectangle screenBounds = config.getBounds();
                Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(config);
                prefs.putInt("x", 0);
                prefs.putInt("y", screenBounds.height - defaultHeight - screenInsets.bottom);
            }

            ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
            String title = messages.getString("preview");
            String url = "http://localhost/song?admin=true";

            frame = new WebFrame(title, url, prefs, defaultWidth, defaultHeight, 0.2);
            frame.setType(Window.Type.POPUP); // TYPE POPUP will not make the taskbar icon blink because of new window
            frame.setAlwaysOnTop(true);
            frame.setVisible(true);
        }
    }

    public boolean isVisible() {
        return frame != null && frame.isVisible();
    }

    @Override
    public void start() {
        boolean visible = prefs.getBoolean("show_preview", true);
        setVisible(visible);
    }

    @Override
    public void stop() {
        // System.exit(0) -> stop() -> isVisible() will still be true even if window is not visible (for some reason)
        prefs.putBoolean("show_preview", isVisible());
    }
}
