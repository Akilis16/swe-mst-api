package com.project.mst.module.tictactoe;

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

@Table(name="tic_tac_toe_move")
@Entity(name="tic_tac_toe_move")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicTacToeMoveModel extends Model {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    protected TicTacToeModel match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    protected UserModel player;

    @Column(name = "position_x", nullable = false)
    protected Integer position_x;

    @Column(name = "position_y", nullable = false)
    protected Integer position_y;

    @Column(name = "move_number", nullable = false)
    protected Integer move_number;

    @Column(name = "timestamp")
    protected Date timestamp;
}
