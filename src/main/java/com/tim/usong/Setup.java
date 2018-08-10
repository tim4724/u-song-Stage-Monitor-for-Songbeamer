package com.tim.usong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

class Setup {

    private Setup() {
    }

    /**
     * On the first start this method will copy some required files to the local directory:
     * - usong.yml is a config file i.e. for http and logging settings
     * - SBRemoteSender.exe is an application to receive actions from songbeamer about current song and current page
     **/
    static void setUpRequiredExternalFiles() {
        Logger logger = LoggerFactory.getLogger(Setup.class);
        ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");

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
        } catch (Exception e) {
            // application may still work if SBRemoteSender is started manually or if yml is provided via parameter
            logger.error("Setup failed", e);
            USongApplication.showErrorDialogAsync(messages.getString("createFilesError"), e);
        }
    }

    static void setUpUI() {
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

        // Frame for all dialogs, where no frame is provided
        Frame rootFrame = new Frame();
        rootFrame.setAlwaysOnTop(true);
        JOptionPane.setRootFrame(rootFrame);
    }
}