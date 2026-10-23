package com.project.mst.module.sudoku;

import com.project.mst.infra.database.model.Model;
import com.project.mst.module.users.UserModel;

import java.util.Date;

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

@Table(name="sudoku_attempt")
@Entity(name="sudoku_attempt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SudokuAttemptModel extends Model {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    protected UserModel user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "puzzle_id", nullable = false)
    protected SudokuModel puzzle;

    @Column(name = "started_at")
    protected Date started_at;

    @Column(name = "finished_at")
    protected Date finished_at;

    @Column(name = "solved")
    protected Boolean solved;

    @Column(name = "moves_count")
    protected Integer moves_count;

    @Column(name = "time_spent_seconds")
    protected Integer time_spent_seconds;
}
