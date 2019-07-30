package com.tim.usong.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import java.util.prefs.Preferences;

public class WebFrame {
    private final Preferences prefs;
    private final String url;

    WebFrame(String title, String url, Preferences prefs, int width, int height, double zoom) {
        this.url = url;
        this.prefs = prefs;
        new Thread(() -> {
            final Display display = new Display();
            final Shell shell = new Shell(display, SWT.ON_TOP | SWT.TITLE | SWT.MIN | SWT.RESIZE);
            shell.setImage(new Image(shell.getDisplay(), getClass().getResourceAsStream("/icon-small2.png")));
            shell.setText(title);
            shell.setBounds(0, 0, width, height);
            shell.setLayout(new FillLayout());

            final Browser browser = new Browser(shell, SWT.NONE);
            browser.setJavascriptEnabled(true);

            Menu customMenu = new Menu(browser);
            MenuItem test = new MenuItem(customMenu, SWT.NONE);
            test.setText("Test");
            browser.setMenu(customMenu);

            shell.open();
            browser.setUrl(url);

            while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
            display.dispose();
        }).start();
    }
}