package com.project.mst.module.tictactoe.dto;

import com.project.mst.module.tictactoe.TicTacToeMode;

import jakarta.validation.constraints.NotNull;

public record StartMatchRequest(@NotNull TicTacToeMode mode, Boolean timed) {
}
