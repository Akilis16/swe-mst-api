package com.project.mst.module.game2048.dto;

import com.project.mst.module.game2048.Game2048Direction;

import jakarta.validation.constraints.NotNull;

public record Game2048MoveRequest(@NotNull Game2048Direction direction) {
}
