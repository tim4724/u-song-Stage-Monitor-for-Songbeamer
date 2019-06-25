package com.tim.usong.view;

import com.tim.usong.GlobalPreferences;
import com.tim.usong.util.NetworkHost;
import io.dropwizard.views.View;

import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class TutorialView extends View {
    private final ResourceBundle messages;
    private final String hostname;
    private final String ipAddress;
    private final int screensCount;

    public TutorialView(Locale locale) {
        super("tutorial.ftl");
        this.messages = ResourceBundle.getBundle("MessagesBundle", locale);

        hostname = NetworkHost.getHostname();
        ipAddress = NetworkHost.getHostAddress();
        screensCount = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length;
    }

    public ResourceBundle getMessages() {
        return messages;
    }

    public String getHostname() {
        return hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getFullscreenDisplay() {
        return GlobalPreferences.getFullscreenDisplay();
    }

    public int getScreensCount() {
        return screensCount;
    }
}