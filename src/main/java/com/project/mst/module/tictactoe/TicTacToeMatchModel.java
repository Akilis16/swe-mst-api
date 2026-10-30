package com.project.mst.module.tictactoe;

import com.project.mst.infra.database.model.Model;
import com.project.mst.module.playsessions.PlaySessionModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "tic_tac_toe_match")
@Entity(name = "tic_tac_toe_match")
@Getter
@Setter
@NoArgsConstructor
public class TicTacToeMatchModel extends Model {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "play_session_id", nullable = false, unique = true)
    protected PlaySessionModel playSession;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 20)
    protected TicTacToeMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 10)
    protected TicTacToeResult result;

    @Version
    @Column(name = "version", nullable = false)
    protected Integer version;
}
