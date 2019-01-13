package com.tim.usong.core;

import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class SongbeamerSettings {
    public String version;
    public File songDir;
    public Boolean titleHasOwnPage;
    public Integer maxLinesPerPage;

    private SongbeamerSettings() {
    }

    public static SongbeamerSettings readSongbeamerSettings() {
        SongbeamerSettings settings = new SongbeamerSettings();
        try {
            settings.readIni();
        } catch (IOException e) {
            Logger logger = LoggerFactory.getLogger(SongbeamerSettings.class);
            logger.error("Error while reading Songbeamer.ini", e);
        }
        try {
            settings.readSBConfig();
        } catch (IOException e) {
            Logger logger = LoggerFactory.getLogger(SongbeamerSettings.class);
            logger.error("Error while reading SBConfig.sfs4", e);
        }
        return settings;
    }

    private void readIni() throws IOException {
        Path iniPath = Paths.get(System.getenv("APPDATA"), "SongBeamer", "SongBeamer.ini");
        List<String> lines;
        try {
            lines = Files.readAllLines(iniPath, StandardCharsets.UTF_16LE);
        } catch (CharacterCodingException e) {
            // Apparently the file can be UTF_16LE or UTF_8 encoeded. yay
            lines = Files.readAllLines(iniPath, StandardCharsets.UTF_8);
        }
        for (String l : lines) {
            if (l.startsWith("FolienBaseDir=")) {
                String songDir = l.replace("FolienBaseDir=", "");
                if (songDir.startsWith("%My Documents%")) {
                    // "%My Documents%" is a Songbeamer variable which points to users documents folder
                    String myDocuments = System.getenv("USERPROFILE") + "\\Documents";
                    songDir = songDir.replace("%My Documents%", myDocuments);
                }
                this.songDir = new File(songDir);
            } else if (l.startsWith("Version=")) {
                version = l.replace("Version=", "");
            } else if (l.startsWith("TitlePosition=")) {
                titleHasOwnPage = l.contains("extrapage");
            }
        }
    }

    private void readSBConfig() throws IOException {
        Path configPath = Paths.get(System.getenv("APPDATA"), "SongBeamer", "SBConfig.sfs4");
        List<String> lines = Files.readAllLines(configPath, Charsets.ISO_8859_1);
        for (String l : lines) {
            int maxLinesIndey = l.indexOf("MaxLinesPerPage");
            if (maxLinesIndey >= 0) {
                maxLinesPerPage = (int) l.charAt(maxLinesIndey + 16);
            }
        }
    }
}
