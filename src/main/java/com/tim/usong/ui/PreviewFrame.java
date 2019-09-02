package com.tim.usong.ui;

import io.dropwizard.lifecycle.Managed;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import java.awt.*;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class PreviewFrame implements Managed {
    private WebFrame frame;
    private final Preferences prefs = Preferences
            .userNodeForPackage(PreviewFrame.class)
            .node("preview");
    private double zoom;

    public synchronized void setVisible(boolean visible) {
        if (isVisible()) {
            frame.close();
            frame = null;
        }
        if (visible) {
            int x = prefs.getInt("x", -1);
            int y = prefs.getInt("y", -1);
            int defaultWidth = 427;
            int height = prefs.getInt("height", 240);

            GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();
            java.awt.Rectangle screenBounds = config.getBounds();

            if (x == -1 && y == -1 || x > screenBounds.width || y > screenBounds.height) {
                Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(config);
                prefs.putInt("x", 0);
                prefs.putInt("y", screenBounds.height - height - screenInsets.bottom);
            }

            ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
            String title = messages.getString("preview");
            String url = "http://localhost/song?admin=true";

            int style = SWT.ON_TOP | SWT.MIN | SWT.RESIZE;
            frame = new WebFrame(title, url, prefs, defaultWidth, height, 3, style) {
                @Override
                public void onBeforeOpen(Shell shell, WebBrowser webBrowser) {
                    shell.addListener(SWT.Resize, event -> {
                        redoLayout(shell, webBrowser);
                    });
                    shell.addDisposeListener(e -> FullScreenStageMonitor.setListener(null));
                    FullScreenStageMonitor.setListener(new FullScreenStageMonitor.FullscreenStageMonitorListener() {
                        @Override
                        public void onShown() {
                            shell.getDisplay().asyncExec(() -> redoLayout(shell, webBrowser));
                        }

                        @Override
                        public void onZoomChange() {
                            shell.getDisplay().asyncExec(() -> redoLayout(shell, webBrowser));
                        }

                        @Override
                        public void onDisposed() {
                            shell.getDisplay().asyncExec(() -> redoLayout(shell, webBrowser));
                        }
                    });
                    webBrowser.setZoomListener(newValue -> {
                        Rectangle fullScreenBounds = FullScreenStageMonitor.getClientArea();
                        if (fullScreenBounds != null && PreviewFrame.this.zoom != newValue) {
                            Rectangle browserArea = webBrowser.getClientArea();
                            double zoom = (newValue * fullScreenBounds.height / browserArea.height
                                    + newValue * fullScreenBounds.width / browserArea.width) / 2;
                            FullScreenStageMonitor.setZoom(zoom);
                        }
                    });
                }
            };
        }
    }

    /**
     * Sync aspect ratio and zoom to the fullscreen stage monitor (if active)
     */
    private void redoLayout(Shell shell, WebBrowser webBrowser) {
        Layout l = shell.getLayout();
        if (!(l instanceof GridLayout)) {
            return;
        }
        ((GridLayout) l).marginHeight = 0;
        ((GridLayout) l).marginWidth = 0;

        Rectangle fullScreenBounds = FullScreenStageMonitor.getClientArea();
        if (fullScreenBounds != null) {
            float desiredRatio = fullScreenBounds.width / (float) fullScreenBounds.height;
            Rectangle bounds = shell.getClientArea();
            float ratio = bounds.width / (float) bounds.height;

            if (ratio > desiredRatio) {
                float offset = bounds.height * ratio - bounds.height * desiredRatio;
                ((GridLayout) l).marginWidth = Math.round(offset / 2f);
            } else {
                float offset = bounds.width / ratio - bounds.width / desiredRatio;
                ((GridLayout) l).marginHeight = Math.round(offset / 2f);
            }
            shell.layout();

            Rectangle browserArea = webBrowser.getClientArea();
            double fullscreenZoom = FullScreenStageMonitor.getZoom();
            zoom = Math.max(1, (fullscreenZoom * browserArea.height / fullScreenBounds.height
                    + fullscreenZoom * browserArea.width / fullScreenBounds.width) / 2);
            webBrowser.setZoom(zoom);
        } else {
            shell.layout();
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
