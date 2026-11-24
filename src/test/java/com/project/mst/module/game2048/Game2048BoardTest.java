package com.project.mst.module.game2048;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

class Game2048BoardTest {

    @Test
    void fusaoSomaPontosEUmaPecaSoFundeUmaVezPorJogada() {
        int[] out = new int[4];
        assertThat(Game2048Board.slideLine(new int[] {2, 2, 2, 2}, out)).isEqualTo(8);
        assertThat(out).containsExactly(4, 4, 0, 0);

        out = new int[4];
        assertThat(Game2048Board.slideLine(new int[] {0, 2, 2, 2}, out)).isEqualTo(4);
        assertThat(out).containsExactly(4, 2, 0, 0);

        out = new int[4];
        assertThat(Game2048Board.slideLine(new int[] {4, 0, 0, 4}, out)).isEqualTo(8);
        assertThat(out).containsExactly(8, 0, 0, 0);
    }

    @Test
    void deslizaNasQuatroDirecoes() {
        List<List<Integer>> rows = List.of(
            List.of(2, 0, 0, 2),
            List.of(0, 4, 0, 0),
            List.of(0, 4, 0, 0),
            List.of(0, 0, 0, 0));

        Game2048Board left = Game2048Board.of(rows);
        assertThat(left.slide(Game2048Direction.LEFT).gained()).isEqualTo(4);
        assertThat(left.rows().get(0)).containsExactly(4, 0, 0, 0);

        Game2048Board right = Game2048Board.of(rows);
        right.slide(Game2048Direction.RIGHT);
        assertThat(right.rows().get(0)).containsExactly(0, 0, 0, 4);
        assertThat(right.rows().get(1)).containsExactly(0, 0, 0, 4);

        Game2048Board up = Game2048Board.of(rows);
        assertThat(up.slide(Game2048Direction.UP).gained()).isEqualTo(8);
        assertThat(up.rows().get(0)).containsExactly(2, 8, 0, 2);

        Game2048Board down = Game2048Board.of(rows);
        down.slide(Game2048Direction.DOWN);
        assertThat(down.rows().get(3)).containsExactly(2, 8, 0, 2);
    }

    @Test
    void jogadaQueNaoMexeNaoContaComoMovimento() {
        Game2048Board board = Game2048Board.of(List.of(
            List.of(2, 4, 0, 0),
            List.of(0, 0, 0, 0),
            List.of(0, 0, 0, 0),
            List.of(0, 0, 0, 0)));

        Game2048Board.Slide slide = board.slide(Game2048Direction.LEFT);

        assertThat(slide.moved()).isFalse();
        assertThat(slide.gained()).isZero();
    }

    @Test
    void inicioTemDuasPecasDoisEPecaNovaSoNasceEmCasaVazia() {
        Game2048Board board = Game2048Board.start(new Random(1));
        long twos = board.rows().stream().flatMap(List::stream).filter(v -> v == 2).count();
        assertThat(twos).isEqualTo(2);

        Game2048Board full = Game2048Board.of(List.of(
            List.of(2, 4, 2, 4),
            List.of(4, 2, 4, 2),
            List.of(2, 4, 2, 4),
            List.of(4, 2, 4, 2)));
        assertThat(full.spawn(new Random(1))).isFalse();
    }

    @Test
    void resultadoVitoriaEm2048EDerrotaSemJogadas() {
        Game2048Board stuck = Game2048Board.of(List.of(
            List.of(2, 4, 2, 4),
            List.of(4, 2, 4, 2),
            List.of(2, 4, 2, 4),
            List.of(4, 2, 4, 2)));
        assertThat(stuck.canMove()).isFalse();
        assertThat(stuck.result()).isEqualTo(Game2048Result.LOSS);

        Game2048Board win = Game2048Board.of(List.of(
            List.of(2048, 0, 0, 0),
            List.of(0, 0, 0, 0),
            List.of(0, 0, 0, 0),
            List.of(0, 0, 0, 0)));
        assertThat(win.result()).isEqualTo(Game2048Result.WIN);

        Game2048Board playing = Game2048Board.of(List.of(
            List.of(2, 2, 0, 0),
            List.of(0, 0, 0, 0),
            List.of(0, 0, 0, 0),
            List.of(0, 0, 0, 0)));
        assertThat(playing.result()).isNull();
    }

    @Test
    void tabuleiroComTamanhoErradoERejeitado() {
        assertThatThrownBy(() -> Game2048Board.of(List.of(List.of(2, 2))))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
