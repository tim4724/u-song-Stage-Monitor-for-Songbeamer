package com.tim.usong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

abstract class Setup {

    private static final Logger logger = LoggerFactory.getLogger(Setup.class.getName());

    static void setUpEverything(boolean showSplash) {
        setUpUI();
        if (showSplash) {
            showSplashScreen();
        }
        try {
            Files.createDirectories(Paths.get(USongApplication.LOCAL_DIR));

            Path configYamlPath = Paths.get(USongApplication.LOCAL_DIR, "usong.yml");
            if (!Files.exists(configYamlPath)) {
                Files.copy(USongApplication.class.getResourceAsStream("/usong.yml"), configYamlPath);
            }

            Path songControlPath = Paths.get(USongApplication.LOCAL_DIR, "uSongControl.jar");
            if (!Files.exists(songControlPath)) {
                Files.copy(USongApplication.class.getResourceAsStream("/uSongControl.jar"), songControlPath);
            }
        } catch (Exception e) {
            logger.error("Error during setup", e);
        }
    }

    private static void setUpUI() {
        Font font = new Font("Helvetica Neue", Font.PLAIN, 14);
        Color darkGrayBg = Color.decode("0x111111");
        Color accent = Color.decode("0x008cff");
        UIManager.put("OptionPane.messageFont", font);
        UIManager.put("OptionPane.buttonFont", font);
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        UIManager.put("OptionPane.background", darkGrayBg);
        UIManager.put("Panel.background", darkGrayBg);
        UIManager.put("Label.foreground", Color.WHITE);
        UIManager.put("Label.font", font);
        UIManager.put("TextArea.foreground", Color.WHITE);
        UIManager.put("TextArea.margin", font);
        UIManager.put("TextArea.background", darkGrayBg);
        UIManager.put("TextArea.font", font);
        UIManager.put("Panel.background", darkGrayBg);
        UIManager.put("Panel.foregrount", Color.WHITE);
        UIManager.put("Button.background", accent);
        UIManager.put("Button.border", BorderFactory.createEmptyBorder(5, 20, 5, 20));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("ProgressBar.background", darkGrayBg);
        UIManager.put("ProgressBar.foreground", accent);
    }

    private static void showSplashScreen() {
        new Thread(() -> {
            String text = String.format("%s %s", USongApplication.APP_NAME, USongApplication.APP_VERSION);
            ImageIcon imageIcon = new ImageIcon(Setup.class.getResource("/icon-small.png"));
            JLabel label = new JLabel(text.toUpperCase(), imageIcon, SwingConstants.CENTER);
            label.setIconTextGap(10);
            label.setHorizontalTextPosition(JLabel.CENTER);
            label.setVerticalTextPosition(JLabel.BOTTOM);

            JProgressBar progressBar = new JProgressBar();
            progressBar.setMaximum(100);
            progressBar.setBorderPainted(false);

            JWindow window = new JWindow();
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