package com.tim.usong.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class WebBrowser {
    final Browser browser;
    private final double defaultZoom;
    private ZoomListener zoomListener;
    private double zoom;

    public WebBrowser(Composite parent, String url, double initialZoom, double defaultZoom) {
        this(parent, url, initialZoom, defaultZoom, null);
    }

    public WebBrowser(Composite parent, String url, double initialZoom, double defaultZoom, ZoomListener listener) {
        this.defaultZoom = defaultZoom;
        this.zoomListener = listener;
        this.zoom = initialZoom;
        browser = new Browser(parent, SWT.NONE);
        browser.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        browser.setJavascriptEnabled(true);

        Menu menu = new Menu(browser);
        onCreateMenu(menu);
        browser.setMenu(menu);

        browser.addTitleListener(event -> {
            // Reset the zoom
            performZoom(0);
            // Prevent zooming at browser level
            browser.execute("window.onkeydown = function (event) {" +
                    "if (event.ctrlKey==true) {event.preventDefault();" +
                    "}}");
            browser.execute("window.onmousewheel = function (event) {" +
                    "if (event.ctrlKey==true) {event.preventDefault();" +
                    "}}");
        });
        browser.addMouseWheelListener(e -> {
            if ((e.stateMask & SWT.CTRL) != 0) {
                performZoom((((double) e.count) / 120.0));
            }
        });
        browser.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.stateMask & SWT.CTRL) != 0) {
                    switch (e.keyCode) {
                        case '=':
                            performZoom(0.025);
                            break;
                        case '-':
                            performZoom(-0.025);
                            break;
                    }
                }
            }
        });

        browser.setUrl(url);
    }

    public void onCreateMenu(Menu menu) {
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
        performZoom(0);
    }

    void addMenuItem(Menu parent, String text, Runnable onSelected) {
        MenuItem item = new MenuItem(parent, SWT.NONE);
        item.setText(text);
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onSelected.run();
            }
        });
    }

    void performZoom(double delta) {
        this.zoom = Math.max(0.1, (zoom / defaultZoom + delta) * defaultZoom);
        if (zoomListener != null) {
            zoomListener.onChanged(this.zoom);
        }
        browser.execute("document.body.parentNode.style.fontSize = \"" + zoom + "px\";" +
                "if(window.fixOverlappingChords) {fixOverlappingChords();}" +
                "if(window.backend && window.backend.fixScroll) {window.backend.fixScroll();}");
    }

    public double getZoom() {
        return zoom;
    }

    public Rectangle getClientArea() {
        return browser.getClientArea();
    }

    public void setZoomListener(ZoomListener zoomListener) {
        this.zoomListener = zoomListener;
    }

    public interface ZoomListener {
        void onChanged(double newValue);
    }
}
