package com.tim.usong.core.ui;

import com.tim.usong.USongApplication;

import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JWindow {
    private static SplashScreen INSTANCE;
    private boolean started = false;
    private boolean error = false;

    public static void showSplash() {
        if (INSTANCE == null) {
            INSTANCE = new SplashScreen();
        }
        INSTANCE.setVisible(true);
    }

    public static void started() {
        INSTANCE.started = true;
    }

    public static void error() {
        INSTANCE.error = true;
    }

    private SplashScreen() {
        String text = String.format("%s %s", USongApplication.APP_NAME, USongApplication.APP_VERSION);
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/icon-small.png"));
        JLabel label = new JLabel(text.toUpperCase(), imageIcon, SwingConstants.CENTER);
        label.setIconTextGap(10);
        label.setHorizontalTextPosition(JLabel.CENTER);
        label.setVerticalTextPosition(JLabel.BOTTOM);
        JProgressBar progressBar = new JProgressBar();
        add(label);
        add(progressBar, BorderLayout.SOUTH);
        setBounds(0, 0, 300, 200);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);

        new Thread(() -> {
            sleep(500);
            for (int i = 0; i < 99 && !error; i++) {
                progressBar.setValue(i + 1);
                sleep(started ? 2 : 16);
            }
            while (!started && !error) {
                sleep(100);
            }
            if (!error) {
                progressBar.setValue(100);
                sleep(250);
                dispose();
                INSTANCE = null;
            }
        }).start();
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignore) {
        }
    }
}
