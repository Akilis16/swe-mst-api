package com.project.mst.module.sudoku;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SudokuValidatorTest {

    static final String SOLUTION =
        "534678912672195348198342567859761423426853791713924856961537284287419635345286179";
    static final String GIVENS =
        "530070000600195000098000060800060003400803001700020006060000280000419005000080079";

    @Test
    void puzzleClassicoEhValidoEUnico() {
        assertThat(SudokuValidator.problems(GIVENS, SOLUTION)).isEmpty();
    }

    @Test
    void tamanhoErradoEhRejeitado() {
        assertThat(SudokuValidator.problems(GIVENS.substring(0, 80), SOLUTION)).isNotEmpty();
    }

    @Test
    void solucaoComRepeticaoEhRejeitada() {
        String broken = "5" + SOLUTION.substring(1, 8) + "5" + SOLUTION.substring(9);
        assertThat(SudokuValidator.problems(GIVENS, broken)).anyMatch(p -> p.contains("repete"));
    }

    @Test
    void givensQueDivergeDaSolucaoEhRejeitado() {
        String broken = "4" + GIVENS.substring(1);
        assertThat(SudokuValidator.problems(broken, SOLUTION)).anyMatch(p -> p.contains("diverge"));
    }

    @Test
    void gradeVaziaAdmiteVariasSolucoes() {
        assertThat(SudokuValidator.problems("0".repeat(81), SOLUTION)).anyMatch(p -> p.contains("mais de uma"));
    }
}
