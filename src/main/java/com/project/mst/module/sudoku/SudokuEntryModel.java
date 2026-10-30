package com.project.mst.module.sudoku;

import com.project.mst.infra.database.model.Model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "sudoku_entry")
@Entity(name = "sudoku_entry")
@Getter
@Setter
@NoArgsConstructor
public class SudokuEntryModel extends Model {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    protected SudokuAttemptModel attempt;

    @Column(name = "cell_idx", nullable = false)
    protected Integer cellIdx;

    @Column(name = "value")
    protected Integer value;

    @Column(name = "correct", nullable = false)
    protected boolean correct;

    @Column(name = "entered_at", nullable = false)
    protected Date enteredAt;
}
