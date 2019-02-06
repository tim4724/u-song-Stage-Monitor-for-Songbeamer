package com.tim.usong.util;

import com.tim.usong.USongApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

public class Browser {
    private Browser() {
    }

    public static void open(String url) {
        try {
            Desktop.getDesktop().browse(new URL(url).toURI());
        } catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(Browser.class);
            logger.error("Failed to open browser", e);
            ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
            USongApplication.showErrorDialogAsync(messages.getString("browserOpenError"), e);
        }
    }
}
