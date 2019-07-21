package com.tim.usong.ui;

import com.tim.usong.util.Browser;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.callback.CefContextMenuParams;
import org.cef.callback.CefMenuModel;
import org.cef.handler.CefContextMenuHandlerAdapter;

import java.awt.*;
import java.util.ResourceBundle;

public class WebView {
    private static CefClient client;
    private CefBrowser browser;

    public WebView(String url) {
        final ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");

        CefApp cefApp;
        if (CefApp.getState() != CefApp.CefAppState.INITIALIZED) {
            CefSettings settings = new CefSettings();
            settings.background_color = settings.new ColorType(255, 0, 0, 0);
            settings.windowless_rendering_enabled = true;
            String[] params = new String[]{
                    "--disable-gpu", "--disable-software-rasterizer", "--no-sandbox"};
            cefApp = CefApp.getInstance(params, settings);
        } else {
            cefApp = CefApp.getInstance();
        }

        client = cefApp.createClient();
        client.addContextMenuHandler(new CefContextMenuHandlerAdapter() {
            @Override
            public void onBeforeContextMenu(CefBrowser browser, CefContextMenuParams params, CefMenuModel model) {
                model.clear();
                model.addItem(0, messages.getString("zoomIncrease"));
                model.addItem(1, messages.getString("zoomDecrease"));
                model.addItem(2, messages.getString("reload"));
                model.addItem(3, messages.getString("openInBrowser"));
            }

            @Override
            public boolean onContextMenuCommand(CefBrowser browser, CefContextMenuParams params, int commandId, int eventFlags) {
                switch (commandId) {
                    case 0:
                        browser.setZoomLevel(browser.getZoomLevel() + 0.5);
                        break;
                    case 1:
                        browser.setZoomLevel(browser.getZoomLevel() - 0.5);
                        break;
                    case 2:
                        browser.reloadIgnoreCache();
                        break;
                    case 3:
                        Browser.open(browser.getURL());
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
        browser = client.createBrowser(url, true, false);
    }

    public void load(String url) {
        if (browser == null) {
            browser = client.createBrowser(url, true, false);
        } else {
            browser.loadURL(url);
        }
    }

    public Component getUIComponent() {
        return browser.getUIComponent();
    }

    public double getZoom() {
        return browser.getZoomLevel();
    }

    public void closeBrowser() {
        if (browser != null) {
            browser.close();
            browser = null;
        }
    }

    public void dispose() {
        client.dispose();
    }
}