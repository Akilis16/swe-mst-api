package com.project.mst.module.tictactoe.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MoveRequest(@NotNull @Min(0) @Max(2) Integer row, @NotNull @Min(0) @Max(2) Integer col) {
}
