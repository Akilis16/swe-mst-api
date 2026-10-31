package com.project.mst.module.tictactoe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class TicTacToeBoardTest {

    @Test
    void xComecaEAsMarcasAlternam() {
        TicTacToeBoard board = new TicTacToeBoard();
        assertThat(board.nextMark()).isEqualTo('X');
        board.place(0, 0, board.nextMark());
        assertThat(board.nextMark()).isEqualTo('O');
        board.place(1, 1, board.nextMark());
        assertThat(board.nextMark()).isEqualTo('X');
    }

    @Test
    void detectaAsOitoLinhasVencedoras() {
        int[][][] lines = {
            {{0, 0}, {0, 1}, {0, 2}}, {{1, 0}, {1, 1}, {1, 2}}, {{2, 0}, {2, 1}, {2, 2}},
            {{0, 0}, {1, 0}, {2, 0}}, {{0, 1}, {1, 1}, {2, 1}}, {{0, 2}, {1, 2}, {2, 2}},
            {{0, 0}, {1, 1}, {2, 2}}, {{0, 2}, {1, 1}, {2, 0}}
        };
        for (int[][] line : lines) {
            TicTacToeBoard board = new TicTacToeBoard();
            for (int[] cell : line) {
                board.place(cell[0], cell[1], 'O');
            }
            assertThat(board.result()).isEqualTo(TicTacToeResult.O_WINS);
        }
    }

    @Test
    void empateComNoveCasasSemVencedor() {
        TicTacToeBoard board = new TicTacToeBoard();
        char[] marks = "XOXXOOOXX".toCharArray();
        for (int i = 0; i < 9; i++) {
            board.place(i / 3, i % 3, marks[i]);
        }
        assertThat(board.winner()).isNull();
        assertThat(board.isFull()).isTrue();
        assertThat(board.result()).isEqualTo(TicTacToeResult.DRAW);
    }

    @Test
    void casaOcupadaNaoAceitaJogada() {
        TicTacToeBoard board = new TicTacToeBoard();
        board.place(1, 1, 'X');
        assertThatThrownBy(() -> board.place(1, 1, 'O')).isInstanceOf(IllegalStateException.class);
        assertThat(board.emptyCells()).hasSize(8);
    }

    @Test
    void tabuleiroEmLinhasUsaNuloNasCasasVazias() {
        TicTacToeBoard board = new TicTacToeBoard();
        board.place(0, 2, 'X');
        assertThat(board.rows().get(0)).containsExactly(null, null, "X");
        assertThat(board.result()).isNull();
    }
}
