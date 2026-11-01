package com.project.mst.module.sudoku;

import java.util.List;

public final class SudokuGrid {

    private final int[] givens;
    private final int[] cells;

    private SudokuGrid(int[] givens) {
        this.givens = givens;
        this.cells = givens.clone();
    }

    public static SudokuGrid of(String givens, List<SudokuEntryModel> entries) {
        SudokuGrid grid = new SudokuGrid(SudokuValidator.digits(givens));
        for (SudokuEntryModel entry : entries) {
            if (!grid.isGiven(entry.getCellIdx())) {
                grid.cells[entry.getCellIdx()] = entry.isCorrect() && entry.getValue() != null ? entry.getValue() : 0;
            }
        }
        return grid;
    }

    public boolean isGiven(int cellIdx) {
        return givens[cellIdx] != 0;
    }

    public void set(int cellIdx, int value) {
        cells[cellIdx] = value;
    }

    public int remainingCells() {
        int remaining = 0;
        for (int cell : cells) {
            if (cell == 0) {
                remaining++;
            }
        }
        return remaining;
    }

    public String toRowMajor() {
        StringBuilder out = new StringBuilder(81);
        for (int cell : cells) {
            out.append(cell);
        }
        return out.toString();
    }
}
