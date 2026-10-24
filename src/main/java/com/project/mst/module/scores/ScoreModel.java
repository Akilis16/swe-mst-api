package com.project.mst.module.scores;

import com.project.mst.infra.database.model.Model;
import com.project.mst.module.games.GameModel;
import com.project.mst.module.playsessions.PlaySessionModel;
import com.project.mst.module.users.UserModel;

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

@Table(name="score")
@Entity(name="score")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScoreModel extends Model {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    protected UserModel user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    protected GameModel game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "play_session_id")
    protected PlaySessionModel play_session;

    @Column(name = "score_value", nullable = false)
    protected Integer score_value;

    @Column(name = "rank_context")
    protected String rank_context;
}
