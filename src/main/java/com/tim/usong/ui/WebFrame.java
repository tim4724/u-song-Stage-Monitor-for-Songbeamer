package com.tim.usong.ui;

import com.google.common.base.Strings;
import com.tim.usong.util.Browse;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class WebFrame {
    private Shell shell;
    private WebBrowser webBrowser;

    WebFrame(String title, String url, Preferences prefs, int defaultWidth, int defaultHeight, double defaultZoom) {
        this(title, url, prefs, defaultWidth, defaultHeight, defaultZoom, SWT.SHELL_TRIM);
    }

    WebFrame(String title, String url, Preferences prefs, int defaultWidth, int defaultHeight, double defaultZoom,
             int style) {
        int width = prefs.getInt("width", defaultWidth);
        int height = prefs.getInt("height", defaultHeight);
        double zoom = prefs.getDouble("zoom2", defaultZoom);

        new Thread(() -> {
            Display display = new Display();
            shell = new Shell(display, style);
            shell.setImage(new Image(display, getClass().getResourceAsStream("/icon-small2.png")));
            shell.setText(Strings.nullToEmpty(title));
            shell.setSize(width, height);
            shell.setBackground(new Color(Display.getCurrent(), 0, 0, 0));

            GridLayout layout = new GridLayout();
            layout.marginHeight = layout.marginWidth = 0;
            shell.setLayout(layout);

            webBrowser = new WebBrowser(shell, url, zoom, defaultZoom) {
                @Override
                public void onCreateMenu(Menu menu) {
                    ResourceBundle msgs = ResourceBundle.getBundle("MessagesBundle");
                    addMenuItem(menu, msgs.getString("zoomIncrease"), () -> performZoom(0.025));
                    addMenuItem(menu, msgs.getString("zoomDecrease"), () -> performZoom(-0.025));
                    addMenuItem(menu, msgs.getString("reload"), browser::refresh);
                    addMenuItem(menu, msgs.getString("openInBrowser"), () -> Browse.open(browser.getUrl()));
                    addMenuItem(menu, msgs.getString("close"), shell::close);
                }
            };
            webBrowser.setZoomListener(newValue -> prefs.putDouble("zoom2", newValue));
            shell.addListener(SWT.Resize, event -> {
                Rectangle bounds = shell.getBounds();
                prefs.putInt("height", bounds.height);
                prefs.putInt("width", bounds.width);
            });

            onBeforeOpen(shell, webBrowser);
            shell.open();
            while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
            display.dispose();
        }).start();
    }

    public void onBeforeOpen(Shell shell, WebBrowser webBrowser) {
    }

    public void close() {
        Shell shell = this.shell;
        if (shell != null && !shell.isDisposed()) {
            shell.getDisplay().asyncExec(shell::close);
        }
    }

    public boolean isDisposed() {
        return shell == null || shell.isDisposed();
    }
}