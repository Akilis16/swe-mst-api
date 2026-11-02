package com.project.mst.module.hangman.dto;

import java.util.List;
import java.util.UUID;

import com.project.mst.module.hangman.HangmanResult;
import com.project.mst.module.scores.dto.ScoreResponse;

public record HangmanRoundResponse(
    UUID roundId,
    UUID playSessionId,
    String category,
    String maskedWord,
    int length,
    int errors,
    int maxErrors,
    List<String> usedLetters,
    boolean finished,
    HangmanResult result,
    String word,
    ScoreResponse score
) {
}
