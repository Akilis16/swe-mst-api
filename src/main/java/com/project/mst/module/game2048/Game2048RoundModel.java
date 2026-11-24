package com.project.mst.module.game2048;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Table(name = "game_2048_round")
@Entity(name = "game_2048_round")
@Getter
@Setter
@NoArgsConstructor
public class Game2048RoundModel extends Model {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "play_session_id", nullable = false, unique = true)
    protected PlaySessionModel playSession;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "board", columnDefinition = "jsonb", nullable = false)
    protected String board;

    @Column(name = "points", nullable = false)
    protected Integer points = 0;

    @Column(name = "moves", nullable = false)
    protected Integer moves = 0;

    @Column(name = "max_tile", nullable = false)
    protected Integer maxTile = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 10)
    protected Game2048Result result;

    @Version
    @Column(name = "version", nullable = false)
    protected Integer version;
}
