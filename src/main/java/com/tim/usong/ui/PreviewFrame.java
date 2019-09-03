package com.tim.usong.ui;

import com.tim.usong.GlobalPreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import java.awt.*;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class PreviewFrame {
    private static WebFrame frame;
    private static double zoom;

    public static synchronized void setVisible(boolean visible) {
        if (isVisible()) {
            frame.close();
            frame = null;
        }
        if (visible) {
            GlobalPreferences.setShowPreview(true);
            Preferences prefs = Preferences
                    .userNodeForPackage(PreviewFrame.class)
                    .node("preview");

            int defaultWidth = 427;
            int height = prefs.getInt("height", 240);

            String title = ResourceBundle.getBundle("MessagesBundle").getString("preview");
            String url = "http://localhost/song?admin=true";

            int style = SWT.ON_TOP | SWT.MIN | SWT.RESIZE;
            frame = new WebFrame(title, url, prefs, defaultWidth, height, 3, style) {
                @Override
                public void onBeforeOpen(Shell shell, WebBrowser webBrowser) {
                    int x = prefs.getInt("x", -1);
                    int y = prefs.getInt("y", -1);
                    GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment()
                            .getDefaultScreenDevice().getDefaultConfiguration();
                    if (x == -1 && y == -1) {
                        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(config);
                        x = 0;
                        y = config.getBounds().height - height - screenInsets.bottom;
                    }

                    shell.setLocation(x, y);
                    shell.addListener(SWT.Resize, event -> redoLayout(shell, webBrowser));
                    shell.getDisplay().addFilter(SWT.FocusOut, new Listener() {
                        @Override
                        public void handleEvent(Event event) {
                            Rectangle bounds = shell.getBounds();
                            prefs.putInt("x", bounds.x);
                            prefs.putInt("y", bounds.y);
                        }
                    });
                    shell.addShellListener(new ShellAdapter() {
                        @Override
                        public void shellClosed(ShellEvent e) {
                            GlobalPreferences.setShowPreview(false);
                        }
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
                        prefs.putDouble("zoom2", newValue);
                        // Update the zoom in the fullscreen window
                        Rectangle fullScreenBounds = FullScreenStageMonitor.getClientArea();
                        if (fullScreenBounds != null && PreviewFrame.zoom != newValue) {
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
    private static void redoLayout(Shell shell, WebBrowser webBrowser) {
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
            zoom = Math.max(0.1, (fullscreenZoom * browserArea.height / fullScreenBounds.height
                    + fullscreenZoom * browserArea.width / fullScreenBounds.width) / 2);
            webBrowser.setZoom(zoom);
        } else {
            shell.layout();
        }
    }

    public static boolean isVisible() {
        return frame != null && !frame.isDisposed();
    }
}
