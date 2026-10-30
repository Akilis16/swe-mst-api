package com.project.mst.module.sudoku;

import com.project.mst.infra.database.model.Model;
import com.project.mst.module.playsessions.Difficulty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "sudoku_puzzle")
@Entity(name = "sudoku_puzzle")
@Getter
@Setter
@NoArgsConstructor
public class SudokuPuzzleModel extends Model {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 20)
    protected Difficulty difficulty;

    @NotNull
    @Pattern(regexp = "^[0-9]{81}$", message = "81 dígitos por linha, 0 nas casas vazias")
    @Column(name = "givens", nullable = false, length = 81)
    protected String givens;

    @NotNull
    @Pattern(regexp = "^[1-9]{81}$", message = "81 dígitos de 1 a 9 por linha")
    @Column(name = "solution", nullable = false, length = 81)
    protected String solution;

    @Column(name = "active", nullable = false)
    protected boolean active = true;
}
