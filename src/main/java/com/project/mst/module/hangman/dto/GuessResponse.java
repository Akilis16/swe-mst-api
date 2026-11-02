package com.project.mst.module.hangman.dto;

import java.util.List;

import com.project.mst.module.hangman.HangmanResult;
import com.project.mst.module.scores.dto.ScoreResponse;

public record GuessResponse(
    boolean correct,
    String maskedWord,
    int errors,
    int maxErrors,
    List<String> usedLetters,
    boolean finished,
    HangmanResult result,
    String word,
    ScoreResponse score
) {
}
