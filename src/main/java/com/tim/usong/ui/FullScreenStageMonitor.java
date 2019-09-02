package com.tim.usong.ui;

import com.tim.usong.GlobalPreferences;
import com.tim.usong.util.Browse;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class FullScreenStageMonitor {
    private static final String url = "http://localhost/song";
    private static FullScreenStageMonitor INSTANCE;
    private final Shell shell;

    public static void showOnDisplay(int displayIndex) throws IllegalArgumentException {
        Display display = new Display();
        Monitor[] monitors = display.getMonitors();
        display.dispose();
        if (displayIndex >= monitors.length) {
            throw new IllegalArgumentException("Display not found");
        }

        new Thread(() -> {
            synchronized (FullScreenStageMonitor.class) {
                // Allow only one instance at a time
                if (INSTANCE == null || INSTANCE.shell.isDisposed()) {
                    INSTANCE = new FullScreenStageMonitor(monitors[displayIndex]);
                } else {
                    INSTANCE.shell.setBounds(monitors[displayIndex].getBounds());
                }
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

    private FullScreenStageMonitor(Monitor monitor) {
        Preferences prefs = Preferences.userNodeForPackage(FullScreenStageMonitor.class)
                .node("fullscreenStageMonitor");
        Display display = new Display();
        shell = new Shell(display, SWT.NO_TRIM | SWT.ON_TOP | SWT.NO_FOCUS);
        shell.setLayout(new FillLayout());
        shell.setBounds(monitor.getBounds());

        WebBrowser webBrowser = new WebBrowser(shell, url, prefs.getDouble("zoom2", 16), 16) {
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

        display.disposeExec(() -> prefs.putDouble("zoom2", webBrowser.getZoom()));
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}