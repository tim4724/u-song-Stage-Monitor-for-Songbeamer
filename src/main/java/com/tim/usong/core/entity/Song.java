package com.tim.usong.core.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Song {
    private final String fileName;
    private final String title;
    private final List<Section> sections;
    private final int pageCount;
    private final int lang;
    private final int langCount;
    private final Type type;

    public Song(String title, Type type) {
        this(null, title, new ArrayList<>(), 1, 1, type);
    }

    public Song(String fileName, String title, List<Section> sections, int lang, int langCount, Type type) {
        this.fileName = fileName;
        this.title = title;
        this.sections = sections;
        this.pageCount = sections.stream().mapToInt(s -> s.getPages().size()).sum();
        this.lang = lang;
        this.langCount = langCount;
        this.type = type;
    }

    public Song(String title, Exception e) {
        this(null, title, new ArrayList<>(), 1, 1, Type.ERROR);
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

    public int getPageCount() {
        return pageCount;
    }

    public int getLang() {
        return lang;
    }

    public int getLangCount() {
        return langCount;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(title, song.title) && Objects.equals(sections, song.sections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, sections, langCount);
    }

    public enum Type {
        SNG, // Real .sng song from file
        ERROR, // Error message
        INFO,// Information, like "no song selected"
        CLOCK
    }
}
