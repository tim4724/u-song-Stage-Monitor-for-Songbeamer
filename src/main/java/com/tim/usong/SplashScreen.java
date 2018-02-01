package com.tim.usong;

import javax.swing.*;
import java.awt.*;

abstract class SplashScreen {
    static void showSplashScreen() {
        new Thread(() -> {
            JWindow window = new JWindow();

            String text = String.format("%s %s", USongApplication.APP_NAME, USongApplication.APP_VERSION);
            ImageIcon imageIcon = new ImageIcon(SplashScreen.class.getResource("/icon-small.png"));
            JLabel label = new JLabel(text.toUpperCase(), imageIcon, SwingConstants.CENTER);
            label.setIconTextGap(10);
            label.setHorizontalTextPosition(JLabel.CENTER);
            label.setVerticalTextPosition(JLabel.BOTTOM);

            JProgressBar progressBar = new JProgressBar();
            progressBar.setMaximum(100);
            progressBar.setBorderPainted(false);

            window.getContentPane().add(label);
            window.getContentPane().add(progressBar, BorderLayout.SOUTH);
            window.setBounds(0, 0, 300, 200);
            window.setLocationRelativeTo(null);
            window.setAlwaysOnTop(true);
            window.setVisible(true);

            sleep(500);
            for (int i = 0; i < 100; i++) {
                progressBar.setValue(i + 1);
                sleep(15);
            }
            sleep(500);

            window.setVisible(false);
        }).start();
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignore) {
        }
    }
}