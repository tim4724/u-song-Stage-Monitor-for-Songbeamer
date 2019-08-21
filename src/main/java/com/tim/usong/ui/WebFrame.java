package com.tim.usong.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
        int x = prefs.getInt("x", -1);
        int y = prefs.getInt("y", -1);

        new Thread(() -> {
            final Display display = new Display();
            final Shell shell = new Shell(display, SWT.ON_TOP | SWT.TITLE | SWT.MIN | SWT.RESIZE | SWT.NO_FOCUS);
            shell.setImage(new Image(display, getClass().getResourceAsStream("/icon-small2.png")));
            shell.setText(title);
            shell.setSize(width, height);
            if (x != -1 || y != -1) {
                shell.setLocation(x, y);
            }
            shell.setLayout(new FillLayout());

            final Browser browser = new Browser(shell, SWT.NO_FOCUS | SWT.NO_SCROLL);
            browser.setJavascriptEnabled(true);

            Menu customMenu = new Menu(browser);
            MenuItem test = new MenuItem(customMenu, SWT.NONE);
            test.setText("&Test");
            test.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    System.out.println(e);
                }
            });
            browser.setMenu(customMenu);

            shell.open();
            browser.setUrl(url);

            // Prevent zooming at browser level
            browser.addProgressListener(new ProgressAdapter() {
                @Override
                public void completed(ProgressEvent event) {
                    browser.execute("window.onkeydown = function (event) {" +
                            "if (event.ctrlKey==true) {event.preventDefault();" +
                            "}}");
                    browser.execute("window.onmousewheel = function (event) {" +
                            "if (event.ctrlKey==true) {event.preventDefault();" +
                            "}}");
                }
            });
            browser.addMouseWheelListener(e -> {
                if ((e.stateMask & SWT.CTRL) != 0) {
                    zoom(browser, e.count);
                }
            });
            browser.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if ((e.stateMask & SWT.CTRL) != 0) {
                        switch (e.keyCode) {
                            case '=':
                                zoom(browser, 3);
                                break;
                            case '-':
                                zoom(browser, -3);
                                break;
                        }
                    }
                }
            });
            /*
            Listener onKeyDown = e -> {
                if ((e.stateMask & SWT.CTRL) != 0) {
                    e.type = SWT.None;
                    e.doit = false;

                    browser.setEnabled(false);
                    System.out.println("browser enabled false");
                }
                browser.setEnabled(false);
                System.out.println("browser enabled false");
            };
            Listener onKeyUp = e -> {
                e.type = SWT.None;
                e.doit = false;
                browser.setEnabled(true);
                System.out.println("browser enabled true");
            };
            display.addFilter(SWT.KeyDown, onKeyDown);
            display.addFilter(SWT.MouseVerticalWheel, onKeyDown);
            display.addFilter(SWT.KeyUp, onKeyUp);
            */
            // IE screen.deviceXDPI / screen.logicalXDPI
            // window.devicePixelRatio

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

    public static void zoom(Browser browser, int delta) {
        System.out.println("Zoom " + delta);
        browser.execute("document.body.style.zoom = 0.5");
    }
}