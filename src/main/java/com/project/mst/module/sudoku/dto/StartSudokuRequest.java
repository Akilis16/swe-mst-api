package com.project.mst.module.sudoku.dto;

import com.project.mst.module.playsessions.Difficulty;

import jakarta.validation.constraints.NotNull;

public record StartSudokuRequest(@NotNull Difficulty difficulty, Integer timeLimitSeconds) {
}
