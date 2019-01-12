package com.tim.usong.core;

import com.google.common.base.Strings;
import com.tim.usong.USongApplication;
import com.tim.usong.core.entity.Page;
import com.tim.usong.core.entity.Section;
import com.tim.usong.core.entity.Song;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.*;
import java.util.prefs.Preferences;

public class SongParser {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle");
    private final Map<String, Integer> langMap = new HashMap<>();
    private final String songDirPath;

    // TODO: Allow user to select the value, or extract it from songbeamer settings

    // Text will be on a new page, if it has more than "n" lines
    // This can be selected in songbeamer: "Extras" -> "Options" -> "Song" -> "Maximum text lines"
    private final int maxLinesPerPage;
    // The title can be on the first page
    // This can be selected in songbeamer: "Extras" -> "Options" -> "Song" -> "Title: " -> "on the first page"
    private final boolean titleHasOwnPage;

    public SongParser(File songDir) {
        String songDirPath = songDir.getAbsolutePath();
        if (!songDirPath.endsWith("\\")) {
            songDirPath += "\\";
        }
        this.songDirPath = songDirPath;
        Preferences preferences = Preferences.userNodeForPackage(SongParser.class).node("songparser");
        maxLinesPerPage = Math.max(0, preferences.getInt("max_lines_per_page", 0));
        titleHasOwnPage = preferences.getBoolean("title_has_own_page", false);
    }

    public void setLangForSong(String fileName, int lang) {
        langMap.put(fileName, lang);
    }

    public Song parse(String fileName) {
        if (!fileName.contains(":")) {
            fileName = songDirPath + fileName;
        }
        try {
            return parseSong(fileName);
        } catch (Exception e) {
            logger.error("Failed to parse song", e);
            String msgKey = e instanceof NoSuchFileException ? "fileNotFoundError" : "fileParseError";
            String errorMessage = messages.getString(msgKey);
            USongApplication.showErrorDialogAsync(errorMessage, fileName + "\n" + e);
            return new Song(errorMessage + fileName, e);
        }
    }

    private Song parseSong(String songFile) throws IOException {
        long startTime = System.currentTimeMillis();

        List<String> pages = new ArrayList<>();
        // Read from file and seperate into pages
        try (Scanner scanner = new Scanner(Paths.get(songFile), "ISO-8859-1")) {
            scanner.useDelimiter("-(-)+\\r?\\n");
            while (scanner.hasNext()) {
                pages.add(scanner.next());
            }
        }

        Header header = new Header(pages.remove(0));
        String title = Strings.isNullOrEmpty(header.title)
                ? songFile.replaceAll("songDirPath|.sng$", "")
                : header.title;
        int desiredLang = langMap.getOrDefault(songFile, 1);

        List<Section> sections = parseSongText(pages, header.langCount, desiredLang);

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
            if (finalSectionList.stream().noneMatch(s2 -> s == s2)) {
                finalSectionList.add(s);
            }
        }

        if (finalSectionList.size() == 1 && Strings.isNullOrEmpty(finalSectionList.get(0).getName())) {
            Section onlySection = finalSectionList.remove(0);
            onlySection.getPages().forEach(p -> finalSectionList.add(new Section("", p)));
        }

        if (titleHasOwnPage) {
            // the first page is the title page.
            // Therefore we add an empty page to our song, to keep in sync with songbeamer
            finalSectionList.get(0).addPage(0, new Page());
        }

        long duration = System.currentTimeMillis() - startTime;
        logger.info(String.format("Parsed song \"%s (Language %d)\" in %dms", title, desiredLang, duration));
        return new Song(songFile, title, finalSectionList, desiredLang, header.langCount, Song.Type.SNG);
    }

    /**
     * Group pages into sections
     *
     * @return List of sections. Every section holds one or many pages
     */
    private List<Section> parseSongText(List<String> pages, int langCount, int desiredLang) {
        List<Section> allSections = new ArrayList<>();
        String currentSectionName = "";

        for (int i = 0; i < pages.size(); i++) {
            String page = pages.get(i);
            // Because of a feature "Maximale Zeilenanzahl" (maximum line count)
            // One page may actuality be split into multiple pages, when presented
            List<Page> newPages = new ArrayList<>();

            // counts all textlines inclusive empty ones. Do not include section names
            int lineCounter = 0;
            // counts only not empty text lines. Do not include section names
            int lineCounterNotEmpty = 0;

            // limit "-1" -> do not discard any empty strings at the end
            String[] lines = page.split("\\r?\\n", -1);

            // iterate, but ignore last element, because its empty always
            for (int j = 0; j < lines.length - 1; j++) {
                String line = lines[j].trim();
                if (isBlockName(line)) {
                    // Vers 22x -> Vers 2, because the "2x" means repeat 2 times.
                    currentSectionName = line.replaceAll("[0-9]x$", "");
                    continue;
                }

                if (lineCounter == 0 || (maxLinesPerPage > 0 && lineCounter % maxLinesPerPage == 0)) {
                    // if something should be a new page, according to the maxLinesPerPage setting
                    // is determined by counting lines, inclusively empty ones.
                    // the upcoming text is part of a new page
                    newPages.add(new Page());
                }

                // lang is determined by counting not empty lines
                int lang = ((lineCounterNotEmpty) % langCount) + 1;

                // substring(0, 4) because of bug with special characters
                if (line.length() >= 4 && line.substring(0, 4).matches("^#?#[0-9] (.)*")) {
                    // language is set explicitly with "#1" or "##1"
                    lang = Integer.parseInt(line.substring(0, line.indexOf(" ")).replaceAll("#", ""));
                    line = line.substring(line.indexOf(" ")).trim();
                }

                if (lang == desiredLang) {
                    lastOf(newPages).addLine(line);
                }

                if (!line.isEmpty()) {
                    lineCounterNotEmpty++;
                }
                lineCounter++;
            }

            // Somehow there is an exception if the last page in the song contains only 1 line.
            // Then this line is added to the previous page
            // But only if the last page is a result of "maxLinesPerPage"
            boolean lastPageOfSong = (i == (pages.size() - 1));
            if (lastPageOfSong && newPages.size() > 1) {
                //TODO: Different compared to songbeamer in some rare cases with empty lines
                Page lastPage = lastOf(newPages);
                String lastPageText = lastPage.getContent();
                if (!lastPageText.contains("\n") && lines.length > 1) {
                    newPages.remove(newPages.size() - 1);
                    lastOf(newPages).addLine(lastPageText);
                }
            }

            Section lastSection = lastOf(allSections);
            if (lastSection == null || !currentSectionName.equals(lastSection.getName())) {
                lastSection = new Section(currentSectionName, newPages);
                allSections.add(lastSection);
            } else {
                lastSection.addPages(newPages);
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
                    logger.error("Failed to parse song-header", e);
                }
            }
        }

        private String getValue(String line) {
            return line.substring(line.indexOf("=") + 1);
        }
    }

    private <T> T lastOf(List<T> list) {
        return !list.isEmpty() ? list.get(list.size() - 1) : null;
    }

    public String getSongDirPath() {
        return songDirPath;
    }
}
