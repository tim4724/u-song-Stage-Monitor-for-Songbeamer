package com.tim.usong.ui;

import com.tim.usong.GlobalPreferences;

import javax.swing.*;
import java.io.File;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class SelectSongDirectoryDialog extends JFileChooser {
    private static final ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");

    public SelectSongDirectoryDialog() {
        setDialogTitle(messages.getString("songDirSelect"));
        setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String path = GlobalPreferences.getSongDir();
        if(path == null) {
            path = System.getProperty("user.home");
        }
        File defaultFile;
        if (path != null && (defaultFile = new File(path)).exists()) {
            setSelectedFile(defaultFile);
        }
    }

    public File getDirectory() {
        int returnVal = showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return getSelectedFile();
        }
        return null;
    }
}
