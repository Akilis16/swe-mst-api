package com.project.mst.module.sudoku.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EntryRequest(@NotNull @Min(0) @Max(80) Integer cellIdx, @Min(1) @Max(9) Integer value) {
}
