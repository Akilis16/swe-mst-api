package com.project.mst.module.game2048;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Tabuleiro 4x4 do 2048 com as regras do jogo original: as peças deslizam até a borda,
 * duas peças iguais vizinhas viram uma com o dobro do valor (uma fusão por peça a cada jogada)
 * e o valor da fusão soma na pontuação. Depois de uma jogada que mexe o tabuleiro nasce um 2.
 */
public final class Game2048Board {

    public static final int SIZE = 4;
    public static final int WIN_TILE = 2048;
    public static final int SPAWN_VALUE = 2;
    public static final int INITIAL_TILES = 2;

    private final int[][] cells = new int[SIZE][SIZE];

    public record Slide(boolean moved, int gained) {
    }

    public static Game2048Board of(List<List<Integer>> rows) {
        if (rows == null || rows.size() != SIZE) {
            throw new IllegalArgumentException("Tabuleiro precisa ter " + SIZE + " linhas");
        }
        Game2048Board board = new Game2048Board();
        for (int r = 0; r < SIZE; r++) {
            List<Integer> row = rows.get(r);
            if (row == null || row.size() != SIZE) {
                throw new IllegalArgumentException("Linha " + r + " precisa ter " + SIZE + " casas");
            }
            for (int c = 0; c < SIZE; c++) {
                Integer value = row.get(c);
                board.cells[r][c] = value == null ? 0 : value;
            }
        }
        return board;
    }

    public static Game2048Board start(Random random) {
        Game2048Board board = new Game2048Board();
        for (int i = 0; i < INITIAL_TILES; i++) {
            board.spawn(random);
        }
        return board;
    }

    public boolean spawn(Random random) {
        List<int[]> empty = emptyCells();
        if (empty.isEmpty()) {
            return false;
        }
        int[] cell = empty.get(random.nextInt(empty.size()));
        cells[cell[0]][cell[1]] = SPAWN_VALUE;
        return true;
    }

    public Slide slide(Game2048Direction direction) {
        boolean moved = false;
        int gained = 0;
        for (int line = 0; line < SIZE; line++) {
            int[] before = read(direction, line);
            int[] after = new int[SIZE];
            gained += slideLine(before, after);
            for (int i = 0; i < SIZE; i++) {
                if (before[i] != after[i]) {
                    moved = true;
                }
            }
            write(direction, line, after);
        }
        return new Slide(moved, gained);
    }

    /** Desliza uma linha para o índice 0 e devolve os pontos das fusões. */
    static int slideLine(int[] line, int[] out) {
        int[] packed = new int[SIZE];
        int size = 0;
        for (int value : line) {
            if (value != 0) {
                packed[size++] = value;
            }
        }
        int gained = 0;
        int target = 0;
        for (int i = 0; i < size; i++) {
            if (i + 1 < size && packed[i] == packed[i + 1]) {
                out[target++] = packed[i] * 2;
                gained += packed[i] * 2;
                i++;
            } else {
                out[target++] = packed[i];
            }
        }
        return gained;
    }

    public boolean canMove() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                int value = cells[r][c];
                if (value == 0) {
                    return true;
                }
                if (c + 1 < SIZE && cells[r][c + 1] == value) {
                    return true;
                }
                if (r + 1 < SIZE && cells[r + 1][c] == value) {
                    return true;
                }
            }
        }
        return false;
    }

    public int maxTile() {
        int max = 0;
        for (int[] row : cells) {
            for (int value : row) {
                max = Math.max(max, value);
            }
        }
        return max;
    }

    public Game2048Result result() {
        if (maxTile() >= WIN_TILE) {
            return Game2048Result.WIN;
        }
        return canMove() ? null : Game2048Result.LOSS;
    }

    public List<List<Integer>> rows() {
        List<List<Integer>> rows = new ArrayList<>(SIZE);
        for (int[] row : cells) {
            List<Integer> values = new ArrayList<>(SIZE);
            for (int value : row) {
                values.add(value);
            }
            rows.add(values);
        }
        return rows;
    }

    private List<int[]> emptyCells() {
        List<int[]> empty = new ArrayList<>();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (cells[r][c] == 0) {
                    empty.add(new int[] {r, c});
                }
            }
        }
        return empty;
    }

    /** Lê a linha na ordem em que as peças andam: o índice 0 é a borda de destino. */
    private int[] read(Game2048Direction direction, int line) {
        int[] values = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            int[] cell = cell(direction, line, i);
            values[i] = cells[cell[0]][cell[1]];
        }
        return values;
    }

    private void write(Game2048Direction direction, int line, int[] values) {
        for (int i = 0; i < SIZE; i++) {
            int[] cell = cell(direction, line, i);
            cells[cell[0]][cell[1]] = values[i];
        }
    }

    private static int[] cell(Game2048Direction direction, int line, int i) {
        return switch (direction) {
            case LEFT -> new int[] {line, i};
            case RIGHT -> new int[] {line, SIZE - 1 - i};
            case UP -> new int[] {i, line};
            case DOWN -> new int[] {SIZE - 1 - i, line};
        };
    }
}
