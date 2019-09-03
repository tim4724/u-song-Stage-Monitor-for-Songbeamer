package com.tim.usong.ui;

import com.tim.usong.GlobalPreferences;
import com.tim.usong.util.Browse;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class FullScreenStageMonitor {
    private static final String url = "http://localhost/song?admin=true";
    private static FullScreenStageMonitor INSTANCE;
    private static FullscreenStageMonitorListener listener;
    private final Preferences prefs = Preferences.userNodeForPackage(FullScreenStageMonitor.class)
            .node("fullscreenStageMonitor");
    private final Thread shutdownHook = new Thread(this::savePrefs);
    private final Shell shell;
    private final Rectangle clientArea;
    private final WebBrowser webBrowser;

    public static void showOnDisplay(int displayIndex) throws IllegalArgumentException {
        Display display = new Display();
        Monitor[] monitors = display.getMonitors();
        display.dispose();
        if (displayIndex >= monitors.length) {
            throw new IllegalArgumentException("Display not found");
        }

        close();
        new Thread(() -> {
            synchronized (FullScreenStageMonitor.class) {
                // Allow only one instance at a time
                INSTANCE = new FullScreenStageMonitor(monitors[displayIndex]);
                if (listener != null) {
                    listener.onShown();
                }
                INSTANCE.loop();
                if (listener != null) {
                    listener.onDisposed();
                }
                INSTANCE = null;
            }
        }).start();
    }

    public static boolean isDisplaying() {
        FullScreenStageMonitor instance = INSTANCE;
        return instance != null && !instance.shell.isDisposed();
    }

    public static void close() {
        FullScreenStageMonitor instance = INSTANCE;
        if (instance != null && instance.shell != null && !instance.shell.isDisposed()) {
            instance.shell.getDisplay().asyncExec(instance.shell::close);
        }
    }

    public static void setListener(FullscreenStageMonitorListener listener) {
        FullScreenStageMonitor.listener = listener;
    }

    public static Rectangle getClientArea() {
        FullScreenStageMonitor instance = INSTANCE;
        if (instance != null && !instance.shell.isDisposed()) {
            return instance.clientArea;
        }
        return null;
    }

    public static double getZoom() {
        FullScreenStageMonitor instance = INSTANCE;
        if (instance != null && !instance.shell.isDisposed()) {
            return instance.webBrowser.getZoom();
        }
        return -1;
    }

    public static void setZoom(double zoom) {
        FullScreenStageMonitor instance = INSTANCE;
        if (instance != null && !instance.shell.isDisposed()) {
            instance.shell.getDisplay().asyncExec(() -> instance.webBrowser.setZoom(zoom));
        }
    }

    private FullScreenStageMonitor(Monitor monitor) {
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        shell = new Shell(new Display(), SWT.NO_TRIM | SWT.ON_TOP | SWT.NO_FOCUS);
        shell.setBounds(monitor.getBounds());
        shell.setLayout(new FillLayout());

        double initialZoom = prefs.getDouble("zoom2", 16);
        WebBrowser.ZoomListener zoomListener = newValue -> {
            if (listener != null) listener.onZoomChange();
        };
        webBrowser = new WebBrowser(shell, url, initialZoom, 16, zoomListener) {
            @Override
            public void onCreateMenu(Menu menu) {
                ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
                addMenuItem(menu, messages.getString("zoomIncrease"), () -> performZoom(0.025));
                addMenuItem(menu, messages.getString("zoomDecrease"), () -> performZoom(-0.025));
                addMenuItem(menu, messages.getString("reload"), browser::refresh);
                addMenuItem(menu, messages.getString("openInBrowser"), () -> Browse.open(browser.getUrl()));
                addMenuItem(menu, messages.getString("closeFullscreenMode"), () -> {
                    GlobalPreferences.setFullscreenDisplay(-1);
                    shell.close();
                });
            }
        };

        shell.addDisposeListener(e -> {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
            savePrefs();
        });
        shell.open();
        clientArea = webBrowser.getClientArea();
    }

    private void loop() {
        Display display = shell.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    private void savePrefs() {
        prefs.putDouble("zoom2", webBrowser.getZoom());
    }

    public interface FullscreenStageMonitorListener {
        void onShown();

        void onZoomChange();

        void onDisposed();
    }
}