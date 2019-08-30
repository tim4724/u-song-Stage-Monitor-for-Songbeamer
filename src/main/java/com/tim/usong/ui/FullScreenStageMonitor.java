package com.tim.usong.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

import java.util.prefs.Preferences;

public class FullScreenStageMonitor {
    private static final Object lock = new Object();
    private static final String url = "http://localhost/song";
    private static FullScreenStageMonitor INSTANCE;
    private final Preferences prefs = Preferences.userNodeForPackage(FullScreenStageMonitor.class)
            .node("fullscreenStageMonitor");
    private final Thread shutdownHook = new Thread(this::savePrefs);
    //private final WebJFXPanel webPanel;
    private final WebBrowser webBrowser;

    public static void showOnDisplay(int displayIndex) throws IllegalArgumentException {
        Display display = new Display();
        Monitor[] monitors = display.getMonitors();
        display.dispose();
        if (displayIndex >= monitors.length) {
            throw new IllegalArgumentException("Display not found");
        }
        synchronized (lock) {
            // Allow only one instance at a time
            if (INSTANCE != null) {
                // INSTANCE.dispose();
            }
            INSTANCE = new FullScreenStageMonitor(monitors[displayIndex]);
        }
    }

    public static boolean isDisplaying() {
        FullScreenStageMonitor instance = INSTANCE;
        return instance != null;// && instance.isVisible();
    }

    public static void close() {
        synchronized (lock) {
            if (INSTANCE != null) {
                // INSTANCE.dispose();
                INSTANCE = null;
            }
        }
    }

    private FullScreenStageMonitor(Monitor monitor) {
        Display display = new Display();
        Shell shell = new Shell(display, SWT.ON_TOP | SWT.NO_FOCUS);
        shell.setImage(new Image(display, getClass().getResourceAsStream("/icon-small2.png")));
        Rectangle monitorBounds = monitor.getBounds();
        shell.setSize(monitorBounds.width, monitorBounds.height);
        shell.setLocation(monitorBounds.x, monitorBounds.y);
        shell.setFullScreen(true);
        shell.setLayout(new FillLayout());
        webBrowser = new WebBrowser(shell, url, 16);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }


    private void savePrefs() {
        // prefs.putDouble("zoom", webPanel.getZoom());
        //  Runtime.getRuntime().removeShutdownHook(shutdownHook);
    }
}