package com.project.mst.module.memory;

import com.project.mst.infra.database.model.Model;
import com.project.mst.module.games.GameModel;

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

@Table(name="card_template")
@Entity(name="card_template")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardTemplateModel extends Model {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    protected GameModel game;

    @Column(name = "image_url", nullable = false)
    protected String image_url;

    @Column(name = "label")
    protected String label;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    protected String metadata;
}
