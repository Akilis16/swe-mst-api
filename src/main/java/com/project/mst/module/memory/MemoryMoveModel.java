package com.project.mst.module.memory;

import com.project.mst.infra.database.model.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "memory_move")
@Entity(name = "memory_move")
@Getter
@Setter
@NoArgsConstructor
public class MemoryMoveModel extends Model {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "round_id", nullable = false)
    protected MemoryRoundModel round;

    @Column(name = "move_number", nullable = false)
    protected Integer moveNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_template_id", nullable = false)
    protected CardTemplateModel cardTemplate;

    @Column(name = "position", nullable = false)
    protected Integer position;

    @Column(name = "matched", nullable = false)
    protected boolean matched;
}
