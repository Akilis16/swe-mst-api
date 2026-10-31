package com.project.mst.module.tictactoe.dto;

import com.project.mst.module.tictactoe.TicTacToeMoveModel;

public record TicTacToeMoveResponse(int number, int row, int col, String mark) {

    public static TicTacToeMoveResponse from(TicTacToeMoveModel move) {
        return new TicTacToeMoveResponse(move.getMoveNumber(), move.getRowIdx(), move.getColIdx(), move.getMark());
    }
}
