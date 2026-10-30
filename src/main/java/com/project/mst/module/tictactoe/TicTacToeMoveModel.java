package com.project.mst.module.tictactoe;

import com.project.mst.infra.database.model.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "tic_tac_toe_move")
@Entity(name = "tic_tac_toe_move")
@Getter
@Setter
@NoArgsConstructor
public class TicTacToeMoveModel extends Model {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    protected TicTacToeMatchModel match;

    @Column(name = "move_number", nullable = false)
    protected Integer moveNumber;

    @Column(name = "row_idx", nullable = false)
    protected Integer rowIdx;

    @Column(name = "col_idx", nullable = false)
    protected Integer colIdx;

    @Column(name = "mark", nullable = false, length = 1)
    protected String mark;
}
