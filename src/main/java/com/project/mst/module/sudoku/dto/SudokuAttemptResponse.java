package com.project.mst.module.sudoku.dto;

import java.util.UUID;

import com.project.mst.module.playsessions.Difficulty;
import com.project.mst.module.scores.dto.ScoreResponse;
import com.project.mst.module.sudoku.SudokuResult;

public record SudokuAttemptResponse(
    UUID attemptId,
    UUID playSessionId,
    Difficulty difficulty,
    String givens,
    String grid,
    int mistakes,
    int mistakesAllowed,
    Integer timeLimitSeconds,
    long elapsedSeconds,
    int remainingCells,
    boolean finished,
    SudokuResult result,
    ScoreResponse score
) {
}
