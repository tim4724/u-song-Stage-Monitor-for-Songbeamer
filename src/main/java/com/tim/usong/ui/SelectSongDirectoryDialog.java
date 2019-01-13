package com.tim.usong.ui;

import javax.swing.*;
import java.io.File;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class SelectSongDirectoryDialog extends JFileChooser {
    private static final ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
    private final Preferences prefs = Preferences.userNodeForPackage(SelectSongDirectoryDialog.class).node("songdir");

    public SelectSongDirectoryDialog() {
        setDialogTitle(messages.getString("songDirSelect"));
        setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String path = prefs.get("manual_song_dir_select", null);
        File defaultFile;
        if (path != null && (defaultFile = new File(path)).exists()) {
            setSelectedFile(defaultFile);
        }
    }

    public File getDirectory() {
        int returnVal = showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = getSelectedFile();
            if (f != null) {
                prefs.put("manualSongDirSelect", f.getAbsolutePath());
            }
            return f;
        }
        return null;
    }
}
