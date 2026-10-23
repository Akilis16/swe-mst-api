package com.project.mst.module.games;

import com.project.mst.infra.database.model.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Table(name="game")
@Entity(name="game")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameModel extends Model {
    @Column(name = "slug", unique = true, nullable = false)
    protected String slug;

    @Column(name = "title", nullable = false)
    protected String title;

    @Column(name = "description", columnDefinition = "text")
    protected String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    protected String metadata;
}
