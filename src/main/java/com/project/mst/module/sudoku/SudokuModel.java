package com.project.mst.module.sudoku;

import com.project.mst.infra.database.model.Model;
import com.project.mst.module.games.GameModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Table(name="sudoku")
@Entity(name="sudoku")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SudokuModel extends Model {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    protected GameModel game;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "initial_grid", columnDefinition = "jsonb")
    protected String initial_grid;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "solution_grid", columnDefinition = "jsonb")
    protected String solution_grid;

    @Column(name = "difficulty")
    protected String difficulty;
}
