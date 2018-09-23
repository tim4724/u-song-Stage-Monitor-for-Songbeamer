package com.tim.usong.core;

import com.tim.usong.core.ui.SelectSongDirectoryDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class SongbeamerSettings {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    public final String version;
    public final File songDir;

    public SongbeamerSettings() throws NoSongDirFoundException {
        // read the directory of songs and the Songbeamer version from Songbeamer.ini
        String sbSongDirPath = null;
        String version = null;
        try {
            SongbeamerIniReader songbeamerIniReader = new SongbeamerIniReader();
            version = songbeamerIniReader.getVersion();
            sbSongDirPath = songbeamerIniReader.getSongDirPath();
        } catch (IOException e) {
            logger.error("Error while reading Songbeamer ini", e);
        }
        this.version = version;

        File songDir;
        if (sbSongDirPath != null && (songDir = new File(sbSongDirPath)).exists()) {
            this.songDir = songDir;
        } else {
            // Fallback if Songbeamer ini could not be parsed
            // Let the user decide which directory to use as songs directory
            songDir = new SelectSongDirectoryDialog().getDirectory();
            if (songDir != null && songDir.exists()) {
                this.songDir = songDir;
            } else {
                throw new NoSongDirFoundException();
            }
        }
    }

    private class SongbeamerIniReader {
        private List<String> lines;

        private SongbeamerIniReader() throws IOException {
            String path = System.getenv("APPDATA") + "\\SongBeamer\\SongBeamer.ini";
            try {
                lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_16LE);
            } catch (CharacterCodingException e) {
                // Apparently the file can be UTF_16LE or UTF_8 encoeded. yay
                lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
            }
        }

        private String getSongDirPath() {
            for (String l : lines) {
                if (l.startsWith("FolienBaseDir=")) {
                    String songDirPath = l.replaceFirst("FolienBaseDir=", "");
                    if (songDirPath.startsWith("%My Documents%")) {
                        // "%My Documents%" is a Songbeamer variable which points to users documents folder
                        String myDocuments = System.getenv("USERPROFILE") + "\\Documents";
                        songDirPath = songDirPath.replace("%My Documents%", myDocuments);
                    }
                    return songDirPath;
                }
            }
            return null;
        }

        private String getVersion() {
            for (String l : lines) {
                if (l.startsWith("Version=")) {
                    return l.replace("Version=", "");
                }
            }
            return null;
        }
    }

    public class NoSongDirFoundException extends Exception {
    }
}
