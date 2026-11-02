package com.project.mst.module.hangman;

import java.util.Collection;
import java.util.Locale;

public final class HangmanWord {

    private HangmanWord() {
    }

    public static String normalizeLetter(String letter) {
        if (letter == null) {
            return null;
        }
        String trimmed = letter.trim().toUpperCase(Locale.ROOT);
        return trimmed.length() == 1 && trimmed.charAt(0) >= 'A' && trimmed.charAt(0) <= 'Z' ? trimmed : null;
    }

    public static String mask(String word, Collection<String> revealed) {
        StringBuilder out = new StringBuilder(word.length() * 2);
        for (int i = 0; i < word.length(); i++) {
            String letter = String.valueOf(word.charAt(i));
            if (i > 0) {
                out.append(' ');
            }
            out.append(revealed.contains(letter) ? letter : "_");
        }
        return out.toString();
    }

    public static boolean isRevealed(String word, Collection<String> revealed) {
        for (int i = 0; i < word.length(); i++) {
            if (!revealed.contains(String.valueOf(word.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

    public static String displayName(String category) {
        if (category == null || category.isBlank()) {
            return category;
        }
        String lower = category.toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
