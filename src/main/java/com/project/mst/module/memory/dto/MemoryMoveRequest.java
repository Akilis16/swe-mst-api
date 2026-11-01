package com.project.mst.module.memory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MemoryMoveRequest(@NotNull @Min(0) Integer position) {
}
