package com.project.mst.module.tictactoe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TicTacToeBoard {

    public static final char X = 'X';
    public static final char O = 'O';
    public static final char EMPTY = ' ';

    private static final int[][] LINES = {
        {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
        {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
        {0, 4, 8}, {2, 4, 6}
    };

    private final char[] cells = new char[9];

    public TicTacToeBoard() {
        Arrays.fill(cells, EMPTY);
    }

    public static TicTacToeBoard fromMoves(List<TicTacToeMoveModel> moves) {
        TicTacToeBoard board = new TicTacToeBoard();
        for (TicTacToeMoveModel move : moves) {
            board.cells[move.getRowIdx() * 3 + move.getColIdx()] = move.getMark().charAt(0);
        }
        return board;
    }

    public boolean isInside(int row, int col) {
        return row >= 0 && row < 3 && col >= 0 && col < 3;
    }

    public boolean isEmpty(int row, int col) {
        return cells[row * 3 + col] == EMPTY;
    }

    public void place(int row, int col, char mark) {
        if (!isInside(row, col)) {
            throw new IllegalArgumentException("Casa fora do tabuleiro");
        }
        if (!isEmpty(row, col)) {
            throw new IllegalStateException("Casa já ocupada");
        }
        cells[row * 3 + col] = mark;
    }

    public int moveCount() {
        int count = 0;
        for (char cell : cells) {
            if (cell != EMPTY) {
                count++;
            }
        }
        return count;
    }

    public char nextMark() {
        return moveCount() % 2 == 0 ? X : O;
    }

    public Character winner() {
        for (int[] line : LINES) {
            char a = cells[line[0]];
            if (a != EMPTY && a == cells[line[1]] && a == cells[line[2]]) {
                return a;
            }
        }
        return null;
    }

    public boolean isFull() {
        return moveCount() == 9;
    }

    public TicTacToeResult result() {
        Character winner = winner();
        if (winner != null) {
            return winner == X ? TicTacToeResult.X_WINS : TicTacToeResult.O_WINS;
        }
        return isFull() ? TicTacToeResult.DRAW : null;
    }

    public boolean isFinished() {
        return result() != null;
    }

    public List<int[]> emptyCells() {
        List<int[]> empty = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            if (cells[i] == EMPTY) {
                empty.add(new int[] {i / 3, i % 3});
            }
        }
        return empty;
    }

    public List<List<String>> rows() {
        List<List<String>> rows = new ArrayList<>(3);
        for (int r = 0; r < 3; r++) {
            List<String> row = new ArrayList<>(3);
            for (int c = 0; c < 3; c++) {
                char cell = cells[r * 3 + c];
                row.add(cell == EMPTY ? null : String.valueOf(cell));
            }
            rows.add(row);
        }
        return rows;
    }
}
