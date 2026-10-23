package com.project.mst.module.assets;

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

@Table(name="asset")
@Entity(name="asset")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssetModel extends Model {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    protected GameModel game;

    @Column(name = "path", nullable = false)
    protected String path;

    @Column(name = "type")
    protected String type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    protected String metadata;
}
