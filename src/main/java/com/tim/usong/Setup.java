package com.tim.usong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

abstract class Setup {

    private static final Logger logger = LoggerFactory.getLogger(Setup.class);

    /**
     * On the first start this method will copy some required files to the local directory:
     * - usong.yml is a config file i.e. for http and logging settings
     * - SBRemoteSender.exe is an application to receive actions from songbeamer about current song and current page
     * - "uSongControl.jar is an application to display and control the song website
     *
     * @param showSplash wether to display a splash screen
     */
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

            Path sbRemoteSenderPath = Paths.get(USongApplication.LOCAL_DIR, "SBRemoteSender.exe");
            if (!Files.exists(sbRemoteSenderPath)) {
                Files.copy(USongApplication.class.getResourceAsStream("/SBRemoteSender.exe"), sbRemoteSenderPath);
            }

            Path songControlPath = Paths.get(USongApplication.LOCAL_DIR, "uSongControl.jar");
            if (!Files.exists(songControlPath)) {
                Files.copy(USongApplication.class.getResourceAsStream("/uSongControl.jar"), songControlPath);
            }
        } catch (Exception e) {
            logger.error("Setup failed", e);
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

    /**
     * Shows a splashscrren of fixed duration.
     */
    private static void showSplashScreen() {
        new Thread(() -> {
            String text = String.format("%s %s", USongApplication.APP_NAME, USongApplication.APP_VERSION);
            ImageIcon imageIcon = new ImageIcon(Setup.class.getResource("/icon-small.png"));
            JLabel label = new JLabel(text.toUpperCase(), imageIcon, SwingConstants.CENTER);
            label.setIconTextGap(10);
            label.setHorizontalTextPosition(JLabel.CENTER);
            label.setVerticalTextPosition(JLabel.BOTTOM);

            JProgressBar progressBar = new JProgressBar();

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
                sleep(16);
            }
            sleep(1000);

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