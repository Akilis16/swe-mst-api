package com.project.mst.module.sudoku;

import java.util.ArrayList;
import java.util.List;

public final class SudokuValidator {

    private SudokuValidator() {
    }

    public static List<String> problems(String givens, String solution) {
        List<String> problems = new ArrayList<>();
        if (givens == null || !givens.matches("^[0-9]{81}$")) {
            problems.add("givens precisa ter 81 dígitos (0 nas casas vazias)");
        }
        if (solution == null || !solution.matches("^[1-9]{81}$")) {
            problems.add("solution precisa ter 81 dígitos de 1 a 9");
        }
        if (!problems.isEmpty()) {
            return problems;
        }

        int[] sol = digits(solution);
        if (!isValidGrid(sol)) {
            problems.add("solution repete dígitos em linha, coluna ou caixa 3x3");
        }

        int[] giv = digits(givens);
        for (int i = 0; i < 81; i++) {
            if (giv[i] != 0 && giv[i] != sol[i]) {
                problems.add("givens diverge da solution na casa " + i);
                break;
            }
        }
        if (!problems.isEmpty()) {
            return problems;
        }

        int solutions = countSolutions(giv.clone(), 2);
        if (solutions == 0) {
            problems.add("givens não tem solução");
        } else if (solutions > 1) {
            problems.add("givens admite mais de uma solução");
        }
        return problems;
    }

    public static boolean isValidGrid(int[] grid) {
        for (int unit = 0; unit < 9; unit++) {
            boolean[] row = new boolean[10];
            boolean[] col = new boolean[10];
            boolean[] box = new boolean[10];
            for (int k = 0; k < 9; k++) {
                int r = grid[unit * 9 + k];
                int c = grid[k * 9 + unit];
                int b = grid[(unit / 3) * 27 + (unit % 3) * 3 + (k / 3) * 9 + (k % 3)];
                if (r == 0 || row[r] || c == 0 || col[c] || b == 0 || box[b]) {
                    return false;
                }
                row[r] = true;
                col[c] = true;
                box[b] = true;
            }
        }
        return true;
    }

    public static int countSolutions(int[] grid, int limit) {
        int empty = -1;
        for (int i = 0; i < 81; i++) {
            if (grid[i] == 0) {
                empty = i;
                break;
            }
        }
        if (empty < 0) {
            return 1;
        }
        int found = 0;
        for (int d = 1; d <= 9; d++) {
            if (fits(grid, empty, d)) {
                grid[empty] = d;
                found += countSolutions(grid, limit - found);
                grid[empty] = 0;
                if (found >= limit) {
                    return found;
                }
            }
        }
        return found;
    }

    private static boolean fits(int[] grid, int idx, int d) {
        int r = idx / 9;
        int c = idx % 9;
        int br = (r / 3) * 3;
        int bc = (c / 3) * 3;
        for (int k = 0; k < 9; k++) {
            if (grid[r * 9 + k] == d || grid[k * 9 + c] == d) {
                return false;
            }
            if (grid[(br + k / 3) * 9 + bc + k % 3] == d) {
                return false;
            }
        }
        return true;
    }

    public static int[] digits(String s) {
        int[] out = new int[81];
        for (int i = 0; i < 81; i++) {
            out[i] = s.charAt(i) - '0';
        }
        return out;
    }
}
