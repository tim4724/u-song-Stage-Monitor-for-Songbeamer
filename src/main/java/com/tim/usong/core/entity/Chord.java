package com.tim.usong.core.entity;

import org.apache.commons.lang3.StringUtils;

public class Chord implements Comparable<Chord> {
    private static final String[] CHORDS = {
            "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };
    private static final String[] CHORDS_ACCIDENTAL = {
            "C", "D<", "D", "E<", "E", "F", "G<", "G", "A<", "A", "B<", "B"
    };
    public final String chord;
    public final float column;

    public Chord(float column, String chord, int transpose, boolean transpAccidental) {
        this.column = column;
        if (transpose == 0 || (transpose + CHORDS.length) % CHORDS.length == 0) {
            this.chord = chord.replaceAll("M", "maj");
            return;
        }

        String[] subChords = chord.split("/");
        for (int i = 0; i < subChords.length; i++) {
            int chordIndex = -1;
            for (int j = CHORDS.length - 1; j >= 0 && chordIndex < 0; j--) {
                if (subChords[i].startsWith(CHORDS[j])) {
                    chordIndex = j;
                }
            }
            for (int j = 0; j < CHORDS_ACCIDENTAL.length && chordIndex < 0; j++) {
                if (subChords[i].startsWith(CHORDS_ACCIDENTAL[j])) {
                    chordIndex = j;
                }
            }
            if (chordIndex < 0) {
                // Not a valid chord. Just ignore it.
                continue;
            }
            int transposedIndex = (chordIndex + transpose + CHORDS.length) % CHORDS.length;
            String newChord = transpAccidental ? CHORDS_ACCIDENTAL[transposedIndex] : CHORDS[transposedIndex];
            subChords[i] = newChord + subChords[i].substring(CHORDS[chordIndex].length());
        }
        this.chord = StringUtils.join(subChords, "/").replaceAll("M", "maj");
    }

    public Chord(float column, String chord) {
        this(column, chord, 0, false);
    }

    @Override
    public int compareTo(Chord o) {
        return Float.compare(column, o.column);
    }

    public String toHtml() {
        StringBuilder htmlChord = new StringBuilder();
        for (char c : chord.toCharArray()) {
            if (c == ' ') {
                htmlChord.append("&nbsp;");
            } else if (c >= '0' && c <= '3') {
                htmlChord.append("&#").append(177 + Character.getNumericValue(c)).append(';');
            } else if (c >= '4' && c <= '9') {
                htmlChord.append("&#").append(8304 + Character.getNumericValue(c)).append(';');
            } else if (c == '<') {
                htmlChord.append("&flat;");
            } else if (c == '=') {
                htmlChord.append("&natur;");
            } else {
                htmlChord.append(c);
            }
        }
        return htmlChord.toString();
    }
}
