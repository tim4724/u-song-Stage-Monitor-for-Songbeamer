package com.tim.usong;

import javax.swing.*;
import java.awt.*;

class SplashScreen {
    public SplashScreen() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JWindow window = new JWindow();

                String text = USongApplication.appName + " " + USongApplication.appVersion;
                JLabel label = new JLabel(text.toUpperCase(), new ImageIcon(getClass().getResource("/icon-small2.png")),
                        SwingConstants.CENTER);
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
                    sleep(12);
                }
                sleep(500);
                window.setVisible(false);
            }
        }).start();
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}