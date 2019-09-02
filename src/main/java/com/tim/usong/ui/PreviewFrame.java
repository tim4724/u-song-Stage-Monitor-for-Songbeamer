package com.tim.usong.ui;

import io.dropwizard.lifecycle.Managed;
import org.eclipse.swt.SWT;

import java.awt.*;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class PreviewFrame implements Managed {
    private WebFrame frame;
    private final Preferences prefs = Preferences
            .userNodeForPackage(PreviewFrame.class)
            .node("preview");

    public synchronized void setVisible(boolean visible) {
        if (isVisible()) {
            frame.close();
            frame = null;
        }
        if (visible) {
            int x = prefs.getInt("x", -1);
            int y = prefs.getInt("y", -1);
            int defaultWidth = 320;
            int height = prefs.getInt("height", 240);

            GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();
            Rectangle screenBounds = config.getBounds();

            if (x == -1 && y == -1 || x > screenBounds.width || y > screenBounds.height) {
                Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(config);
                prefs.putInt("x", 0);
                prefs.putInt("y", screenBounds.height - height - screenInsets.bottom);
            }

            ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
            String title = messages.getString("preview");
            String url = "http://localhost/song?admin=true";

            int style = SWT.ON_TOP | SWT.MIN | SWT.RESIZE;
            frame = new WebFrame(title, url, prefs, defaultWidth, height, 3, style);
        }
    }

    public boolean isVisible() {
        return frame != null && !frame.isDisposed();
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
