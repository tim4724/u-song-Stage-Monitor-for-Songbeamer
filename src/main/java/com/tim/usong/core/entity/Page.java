package com.tim.usong.core.entity;

import java.util.Arrays;
import java.util.Objects;

public class Page {
    private String content = "";

    public Page(String... lines) {
        Arrays.stream(lines).forEach(this::addLine);
    }

    public void addLine(String line) {
        line = line.trim();
        if (!line.isEmpty()) {
            if (!content.isEmpty()) {
                content += "\n<br />";
            }
            content += line;
        }
    }

    public String getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Page page = (Page) o;
        return Objects.equals(content, page.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }
}
