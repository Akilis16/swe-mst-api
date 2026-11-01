package com.project.mst.module.sudoku.dto;

import com.project.mst.module.scores.dto.ScoreResponse;
import com.project.mst.module.sudoku.SudokuResult;

public record EntryResponse(boolean correct, int mistakes, int mistakesAllowed, int remainingCells,
    boolean finished, SudokuResult result, ScoreResponse score) {
}
