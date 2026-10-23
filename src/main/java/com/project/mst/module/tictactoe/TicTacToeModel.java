package com.project.mst.module.tictactoe;

import com.project.mst.infra.database.model.Model;
import com.project.mst.module.games.GameModel;
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

@Table(name="tic_tac_toe")
@Entity(name="tic_tac_toe")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicTacToeModel extends Model {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    protected GameModel game;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_x_id", nullable = false)
    protected UserModel player_x;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_o_id")
    protected UserModel player_o;

    @Column(name = "result")
    protected String result;

    @Column(name = "started_at")
    protected Date started_at;

    @Column(name = "ended_at")
    protected Date ended_at;
}
