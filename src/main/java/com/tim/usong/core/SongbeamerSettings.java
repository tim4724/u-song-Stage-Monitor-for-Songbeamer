package com.tim.usong.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SongbeamerSettings {
    public String version;
    public File songDir;
    public Boolean titleHasOwnPage;

    private SongbeamerSettings() {
    }

    public static SongbeamerSettings readSongbeamerIniFile() {
        SongbeamerSettings settings = new SongbeamerSettings();
        List<String> lines;
        try {
            Path path = Paths.get(System.getenv("APPDATA") + "\\SongBeamer\\SongBeamer.ini");
            try {
                lines = Files.readAllLines(path, StandardCharsets.UTF_16LE);
            } catch (CharacterCodingException e) {
                // Apparently the file can be UTF_16LE or UTF_8 encoeded. yay
                lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            Logger logger = LoggerFactory.getLogger(SongbeamerSettings.class);
            logger.error("Error while reading Songbeamer ini", e);
            return settings;
        }

        for (String l : lines) {
            if (l.startsWith("FolienBaseDir=")) {
                String songDir = l.replaceFirst("FolienBaseDir=", "");
                if (songDir.startsWith("%My Documents%")) {
                    // "%My Documents%" is a Songbeamer variable which points to users documents folder
                    String myDocuments = System.getenv("USERPROFILE") + "\\Documents";
                    songDir = songDir.replace("%My Documents%", myDocuments);
                }
                settings.songDir = new File(songDir);
            } else if (l.startsWith("Version=")) {
                settings.version = l.replace("Version=", "");
            } else if (l.startsWith("TitlePosition=")) {
                settings.titleHasOwnPage = l.contains("extrapage");
            }
        }
        return settings;
    }
}
