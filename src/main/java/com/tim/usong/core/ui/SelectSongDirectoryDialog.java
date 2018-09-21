package com.tim.usong.core.ui;

import javax.swing.*;
import java.io.File;
import java.util.ResourceBundle;

public class SelectSongDirectoryDialog extends JFileChooser {
    private static final ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");

    public SelectSongDirectoryDialog() {
        setDialogTitle(messages.getString("songDirSelect"));
        setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }

    public File getDirectory() {
        int returnVal = showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return getSelectedFile();
        }
        return null;
    }
}
