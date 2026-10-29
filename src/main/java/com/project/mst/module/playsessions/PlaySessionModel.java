package com.project.mst.module.playsessions;

import com.project.mst.infra.database.model.Model;
import com.project.mst.module.games.GameModel;
import com.project.mst.module.users.UserModel;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Table(name = "play_session")
@Entity(name = "play_session")
@Getter
@Setter
@NoArgsConstructor
public class PlaySessionModel extends Model {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    protected UserModel user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    protected GameModel game;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    protected PlaySessionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", length = 20)
    protected Difficulty difficulty;

    @Column(name = "timed", columnDefinition = "boolean default true")
    protected Boolean timed;

    @Column(name = "started_at")
    protected Date startedAt;

    @Column(name = "ended_at")
    protected Date endedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "client_meta", columnDefinition = "jsonb")
    protected String clientMeta;

    @Version
    @Column(name = "version", nullable = false, columnDefinition = "integer default 0 not null")
    protected Integer version;

    public boolean isOpen() {
        if (status != null) {
            return status == PlaySessionStatus.IN_PROGRESS;
        }
        return endedAt == null;
    }
}
