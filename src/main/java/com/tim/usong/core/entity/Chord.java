package com.tim.usong.core.entity;

import com.tim.usong.GlobalPreferences;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class Chord implements Comparable<Chord> {
    private static final String[] CHORDS = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    private static final String[] CHORDS_ACC = {"C", "D<", "D", "E<", "E", "F", "G<", "G", "A<", "A", "B<", "B"};
    public final String chord;
    public final float column;

    public Chord(float column, String chord, int transpose, boolean transpAccidental) {
        this.column = column;
        if ((transpose + CHORDS.length) % CHORDS.length == 0) {
            this.chord = chord.replaceAll("M", "maj");
            return;
        }

        String[] subChords = chord.split("/");
        for (int i = 0; i < subChords.length; i++) {
            String simpleChord = getSimpleChord(subChords[i]);
            int chordIndex = getChordIndex(simpleChord);
            if (chordIndex != -1) {
                int transposedIndex = (chordIndex + transpose + CHORDS.length) % CHORDS.length;
                String newChord = transpAccidental ? CHORDS_ACC[transposedIndex] : CHORDS[transposedIndex];
                if (GlobalPreferences.getChordsUseBNatural() && transposedIndex == 11) {
                    newChord += "=";
                }
                subChords[i] = subChords[i].replace(simpleChord, newChord);
            }
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
        return chord.replaceAll("<", "&flat;").replaceAll("=", "&natur;");
    }

    private static int getChordIndex(String c) {
        String[] allChords = {"C", "D<", "D", "E<", "F<", "F", "G<", "G", "A<", "A", "B<", "C<",
                "B#", "C#", "D", "D#", "E", "E#", "F#", "G", "G#", "A", "A#", "B"};
        return ArrayUtils.indexOf(allChords, c) % CHORDS.length;
    }

    private static String getSimpleChord(String c) {
        return c.substring(0, (c.length() > 1 && (c.charAt(1) == '#' || c.charAt(1) == '<')) ? 2 : 1);
    }
}
