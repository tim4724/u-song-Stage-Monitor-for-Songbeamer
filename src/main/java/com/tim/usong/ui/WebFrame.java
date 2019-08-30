package com.tim.usong.ui;

import com.google.common.base.Strings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.util.prefs.Preferences;

public class WebFrame {
    private final Preferences prefs;
    private final String url;

    WebFrame(String title, String url, Preferences prefs, int width, int height, double zoom) {
        this.url = url;
        this.prefs = prefs;
        int x = prefs.getInt("x", -1);
        int y = prefs.getInt("y", -1);

        new Thread(() -> {
            final Display display = new Display();
            final Shell shell = new Shell(display, SWT.ON_TOP | SWT.TITLE | SWT.MIN | SWT.RESIZE);
            shell.setImage(new Image(display, getClass().getResourceAsStream("/icon-small2.png")));
            shell.setText(Strings.nullToEmpty(title));
            shell.setSize(width, height);
            if (x != -1 || y != -1) {
                shell.setLocation(x, y);
            }
            shell.setLayout(new FillLayout());
            WebBrowser webBrowser = new WebBrowser(shell, url, 16);

            shell.open();
            while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
            display.dispose();

            /*
            prefs.putDouble("zoom", webPanel.getZoom());
            prefs.putInt("height", getHeight());
            prefs.putInt("width", getWidth());
            prefs.putInt("x", getX());
            prefs.putInt("y", getY());
            */
        }
        ).start();
    }
}