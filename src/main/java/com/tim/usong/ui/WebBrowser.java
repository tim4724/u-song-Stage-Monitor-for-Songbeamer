package com.tim.usong.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class WebBrowser {
    private double zoom = 16;

    public WebBrowser(Composite parent, String url, double zoom) {
        Browser browser = new Browser(parent, SWT.NONE);
        browser.setJavascriptEnabled(true);

        Menu customMenu = new Menu(browser);
        MenuItem test = new MenuItem(customMenu, SWT.NONE);
        test.setText("&Close");
        test.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                System.out.println(e);
                browser.getDisplay().close();
            }
        });
        browser.setMenu(customMenu);

        browser.setUrl(url);

        // Prevent zooming at browser level
        browser.addTitleListener(event -> {
            zoom(browser, 0);
            browser.execute("window.onkeydown = function (event) {" +
                    "if (event.ctrlKey==true) {event.preventDefault();" +
                    "}}");
            browser.execute("window.onmousewheel = function (event) {" +
                    "if (event.ctrlKey==true) {event.preventDefault();" +
                    "}}");
        });
        browser.addMouseWheelListener(e -> {
            if ((e.stateMask & SWT.CTRL) != 0) {
                zoom(browser, e.count / 6f);
            }
        });
        browser.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.stateMask & SWT.CTRL) != 0) {
                    switch (e.keyCode) {
                        case '=':
                            zoom(browser, 0.25);
                            break;
                        case '-':
                            zoom(browser, -0.25);
                            break;
                    }
                }
            }
        });
    }

    public void zoom(Browser browser, double delta) {
        zoom = Math.max(1, zoom + delta);
        browser.execute("document.body.parentNode.style.fontSize = \"" + zoom + "px\";" +
                "fixOverlappingChords();" +
                "document.body.style.transition = \"font-size 0.1s linear\";");
    }
}
