package com.tim.usong.core.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Page {
    private final List<Line> lines = new ArrayList<>();

    public Page(String... lines) {
        Arrays.stream(lines).forEach(line -> addLine(line, null));
    }

    public void addLine(String line, List<Chord> chords) {
        // Combine chords, that would end up in the same position
        if (chords != null) {
            for (int i = chords.size() - 1; i > 0; i--) {
                Chord a = chords.get(i - 1);
                Chord b = chords.get(i);
                if (Math.round(a.column) == Math.round(b.column)) {
                    Chord combinedChord = new Chord(b.column, a.chord + b.chord);
                    chords.add(chords.indexOf(a), combinedChord);
                    chords.remove(a);
                    chords.remove(b);
                }
            }
        }
        lines.add(new Line(line, chords));
    }

    public void addLinesFromPage(Page other) {
        this.lines.addAll(other.lines);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Page page = (Page) o;
        return Objects.equals(lines, page.lines);
    }

    public String toHtml() {
        StringBuilder htmlBuilder = new StringBuilder();
        for (Line line : lines) {
            htmlBuilder.append(line.textLine).append("<br>\n");
        }
        return htmlBuilder.toString();
    }

    public String toHtmlWithCords() {
        StringBuilder htmlBuilder = new StringBuilder();
        for (Line line : lines) {
            if (line.chords != null) {
                int offset = htmlBuilder.length();
                htmlBuilder.append(line.textLine);
                for (Chord c : line.chords) {
                    int insertIndex = offset + Math.max(Math.round(c.column), 0);
                    if (c.column < 0) {
                        htmlBuilder.insert(offset, "&nbsp;");
                        offset += 6;
                    }
                    for (int i = 0, l = insertIndex - htmlBuilder.length(); i < l; i++) {
                        htmlBuilder.append("&nbsp;");
                        offset += 6;
                        insertIndex += 5;
                        if (i == l - 1) {
                            String placeHolderText = String.format(
                                    "<span class=\"chord simplePlaceholder\">%s</span>",
                                    c.toHtml());
                            htmlBuilder.append(placeHolderText);
                            offset += placeHolderText.length();
                        }
                    }

                    String insertText = String.format(
                            "<span class=\"chordPos\"><span class=\"chord\">%s</span></span>",
                            c.toHtml());
                    htmlBuilder.insert(insertIndex, insertText);
                    offset += insertText.length();
                }
            } else {
                htmlBuilder.append(line.textLine);
            }
            htmlBuilder.append("<br>\n");
        }
        return htmlBuilder.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(lines);
    }

    public int getLinesCount() {
        return lines.size();
    }

    public boolean hasChords() {
        for (Line l : lines) {
            if (l.chords != null && l.chords.size() > 0) {
                return true;
            }
        }
        return false;
    }

    private static class Line {
        public final String textLine;
        public final List<Chord> chords;

        private Line(String textLine, List<Chord> chords) {
            this.textLine = textLine;
            this.chords = chords;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            return Objects.equals(textLine, ((Line) o).textLine) &&
                    Objects.equals(chords, ((Line) o).chords);
        }

        @Override
        public int hashCode() {
            return Objects.hash(textLine, chords);
        }
    }
}
