package com.project.mst.module.sudoku;

import com.project.mst.infra.database.model.Model;
import com.project.mst.module.playsessions.PlaySessionModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "sudoku_attempt")
@Entity(name = "sudoku_attempt")
@Getter
@Setter
@NoArgsConstructor
public class SudokuAttemptModel extends Model {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "play_session_id", nullable = false, unique = true)
    protected PlaySessionModel playSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "puzzle_id", nullable = false)
    protected SudokuPuzzleModel puzzle;

    @Column(name = "mistakes", nullable = false)
    protected Integer mistakes = 0;

    @Column(name = "time_limit_seconds")
    protected Integer timeLimitSeconds;

    @Column(name = "solved", nullable = false)
    protected boolean solved;
}
