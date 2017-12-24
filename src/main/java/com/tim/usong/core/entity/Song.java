package com.tim.usong.core.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Song {

    private final String fileName;
    private final String title;
    private final List<Section> sections;
    private final int lang;
    private final int langCount;

    public Song(String title) {
        this(null, title, new ArrayList<>(), 1, 1);
    }

    public Song(String fileName, String title, List<Section> sections, int lang, int langCount) {
        this.fileName = fileName;
        this.title = title;
        this.sections = sections;
        this.lang = lang;
        this.langCount = langCount;
    }

    public Song(String title, Exception e) {
        this(null, title, new ArrayList<>(), 1, 1);
        String errString = e.toString().replace("\n", "\n<br />");
        sections.add(new Section(e.getClass().getName(), new Page(errString)));
    }

    public String getTitle() {
        return title;
    }

    public List<Section> getSections() {
        return sections;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLang() {
        return lang;
    }

    public int getLangCount() {
        return langCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(title, song.title) &&
                Objects.equals(sections, song.sections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, sections);
    }
}
