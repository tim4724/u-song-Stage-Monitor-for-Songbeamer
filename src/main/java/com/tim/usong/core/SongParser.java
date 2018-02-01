package com.tim.usong.core;

import com.tim.usong.USongApplication;
import com.tim.usong.core.entity.Page;
import com.tim.usong.core.entity.Section;
import com.tim.usong.core.entity.Song;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.*;

public class SongParser {

    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());
    private final String path;
    private Map<String, Integer> langMap = new HashMap<>();

    public SongParser(String path) {
        this.path = path;
    }

    public void setLangForSong(String songTitle, int lang) {
        langMap.put(songTitle, lang);
    }

    public Song parse(String fileName) {
        if (!fileName.endsWith(".sng")) {
            return Song.noSongSelected;
        }
        if (!fileName.contains(":")) {
            fileName = path + fileName;
        }
        try {
            return parseSong(fileName);
        } catch (Exception e) {
            logger.error(e.getMessage());
            if (e instanceof NoSuchFileException) {
                USongApplication.showErrorDialogAsync("Datei nicht gefunden:\n" + fileName + "\n" + e, true);
                return new Song("Datei nicht gefunden:\n" + fileName, e);
            }
            USongApplication.showErrorDialogAsync("Fehler beim Verarbeiten von " + fileName + "\n" + e, true);
            return new Song("Fehler beim Verarbeiten von " + fileName, e);
        }
    }

    private Song parseSong(String songFile) throws IOException {
        long startTime = System.currentTimeMillis();
        String title = songFile.replace(path, "").replace(".sng", "");
        List<String> pages = new ArrayList<>();

        try (Scanner scanner = new Scanner(Paths.get(songFile), "ISO-8859-1")) {
            scanner.useDelimiter("-(-)+\\r?\\n");
            while (scanner.hasNext()) {
                pages.add(scanner.next());
            }
        }

        Header header = new Header(pages.remove(0));
        if (header.title != null && !header.title.isEmpty()) {
            title = header.title;
        }
        int desiredLang = 1;
        if (langMap.containsKey(title)) {
            desiredLang = langMap.get(title);
        }

        List<Section> sections = parseSongtext(pages, header.langCount, desiredLang);
        List<Section> finalSectionList = new ArrayList<>();

        if (header.verseOrder != null) {
            for (String sectionName : header.verseOrder) {
                Section section = findSectionByName(sections, sectionName);
                if (section != null) {
                    finalSectionList.add(section);
                }
            }
        }

        for (Section s : sections) {
            boolean alreadyAdded = false;
            for (Section s2 : finalSectionList) {
                if (s == s2) {
                    alreadyAdded = true;
                }
            }
            if (!alreadyAdded) {
                finalSectionList.add(s);
            }
        }

        if (finalSectionList.size() == 1) {
            Section onlySection = finalSectionList.remove(0);
            for (Page p : onlySection.getPages()) {
                finalSectionList.add(new Section("", p));
            }
        }

        logger.info(String.format("Parsed song \"%s (Language %d)\" in %dms", title, desiredLang, System.currentTimeMillis() - startTime));
        return new Song(songFile, title, finalSectionList, desiredLang, header.langCount);
    }

    /**
     * Group pages into sections
     *
     * @return List of sections. Every section holds one or many pages
     */
    private List<Section> parseSongtext(List<String> pages, int langCount, int desiredLang) {
        List<Section> allSections = new ArrayList<>();
        String currentSectionName = "";

        for (String page : pages) {
            Page newPage = new Page();

            int lineCounter = 0;
            for (String line : page.split("\\r?\\n")) {
                line = line.trim();
                if (isBlockName(line)) {
                    currentSectionName = line;
                    continue;
                }

                int lang = (lineCounter % langCount) + 1;
                if (line.length() >= 4 && line.substring(0, 4).matches("^#?#[0-9] (.)*")) {//substring(0, 4) because o fbug with special characters
                    lang = Integer.parseInt(line.substring(0, line.indexOf(" ")).replaceAll("#", ""));
                    line = line.substring(line.indexOf(" "));
                }
                if (lang == desiredLang) {
                    newPage.addLine(line.trim());
                }
                lineCounter++;//TODO: May need to be modified if lang ist given explicitly (##1, ##2...)
            }

            Section lastSection = lastOf(allSections);
            if (lastSection == null || !lastSection.getName().equals(currentSectionName)) {
                allSections.add(new Section(currentSectionName, newPage));
            } else {
                lastSection.addPage(newPage);
            }
        }
        return allSections;
    }

    private boolean isBlockName(String blockName) {
        //These are all known block names
        return blockName.matches("(?i)^" +
                "(Unbekannt|Unbenannt|Unknown|Misc|Pre-Coda|Zwischenspiel|Instrumental|" +
                "Interlude|Coda|Ending|Outro|Teil|Part|Chor|Solo|Intro|" +
                "Vers|Verse|Strophe|Pre-Refrain|Refrain|Pre-Chorus|Chorus|Pre-Bridge|Bridge)($| \\S*)");
    }

    private Section findSectionByName(List<Section> sections, String name) {
        return sections.stream().filter(s -> s.getName().equals(name)).findFirst().orElse(null);
    }

    private class Header {
        private int langCount = 1;
        private String title;
        private String[] verseOrder;

        private Header(String in) {
            String lines[] = in.split("\\r?\\n");
            for (String line : lines) {
                try {
                    if (line.startsWith("#LangCount=")) {
                        langCount = Integer.valueOf(getValue(line));
                    } else if (line.startsWith("#Title=")) {
                        title = getValue(line);
                    } else if (line.startsWith("#VerseOrder=")) {
                        verseOrder = getValue(line).split(",");
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }

        private String getValue(String line) {
            return line.substring(line.indexOf("=") + 1, line.length());
        }
    }

    private <T> T lastOf(List<T> list) {
        return !list.isEmpty() ? list.get(list.size() - 1) : null;
    }

    public String getSongDir() {
        return path;
    }

    public int getLangForSong(String songTitle) {
        Integer lang = langMap.get(songTitle);
        if (lang == null) {
            return 1;
        }
        return lang;
    }
}
