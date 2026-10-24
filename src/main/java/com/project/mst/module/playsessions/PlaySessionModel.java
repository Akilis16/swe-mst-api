package com.project.mst.module.playsessions;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Table(name="play_session")
@Entity(name="play_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaySessionModel extends Model {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    protected UserModel user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    protected GameModel game;

    @Column(name = "started_at")
    protected Date started_at;

    @Column(name = "ended_at")
    protected Date ended_at;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "client_meta", columnDefinition = "jsonb")
    protected String client_meta;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result_summary", columnDefinition = "jsonb")
    protected String result_summary;
}
