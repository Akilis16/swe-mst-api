package com.project.mst.module.scores;

import com.project.mst.infra.database.model.Model;
import com.project.mst.module.playsessions.PlaySessionModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Table(name = "score")
@Entity(name = "score")
@Getter
@Setter
@NoArgsConstructor
public class ScoreModel extends Model {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "play_session_id", nullable = false, unique = true)
    protected PlaySessionModel playSession;

    @Column(name = "value", nullable = false)
    protected Integer value;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "breakdown", columnDefinition = "jsonb")
    protected String breakdown;
}
