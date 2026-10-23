package com.project.mst.module.memory;

import com.project.mst.infra.database.model.Model;
import com.project.mst.module.playsessions.PlaySessionModel;

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

@Table(name="memory_move")
@Entity(name="memory_move")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemoryMoveModel extends Model {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "play_session_id", nullable = false)
    protected PlaySessionModel play_session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_template_id", nullable = false)
    protected CardTemplateModel card_template;

    @Column(name = "position", nullable = false)
    protected Integer position;

    @Column(name = "timestamp")
    protected Date timestamp;

    @Column(name = "matched")
    protected Boolean matched;
}
