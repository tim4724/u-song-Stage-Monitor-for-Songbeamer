package com.tim.usong.core.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Section {
    public enum Type {VERSE, PRE_CHORUS, CHORUS, PRE_BRIDGE, BRIDGE, ENDING, UNKNOWN}

    private final String name;
    private final Type type;
    private final List<Page> pages = new ArrayList<>();

    public Section(String name, Page... pages) {
        this(name, Arrays.asList(pages));
    }

    public Section(String name, List<Page> pages) {
        this.name = name;
        if (name.matches("(?i)^(Vers|Verse|Strophe)($| .*)")) {
            type = Section.Type.VERSE;
        } else if (name.matches("(?i)^(Pre-Refrain|Pre-Chorus)($| .*)")) {
            type = Type.PRE_CHORUS;
        } else if (name.matches("(?i)^(Refrain|Chorus)($| .*)")) {
            type = Section.Type.CHORUS;
        } else if (name.matches("(?i)^(Pre-Bridge)($| .*)")) {
            type = Type.PRE_BRIDGE;
        } else if (name.matches("(?i)^(Bridge)($| .*)")) {
            type = Section.Type.BRIDGE;
        } else if (name.matches("(?i)^(Ending|Outro)($| .*)")) {
            type = Type.ENDING;
        } else {
            type = Type.UNKNOWN;
        }
        this.pages.addAll(pages);
    }

    public void addPage(int index, Page page) {
        pages.add(index, page);
    }

    public void addPages(List<Page> pages) {
        this.pages.addAll(pages);
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public List<Page> getPages() {
        return pages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Section section = (Section) o;
        return Objects.equals(name, section.name) &&
                type == section.type &&
                Objects.equals(pages, section.pages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, pages);
    }
}