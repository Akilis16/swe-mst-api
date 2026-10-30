package com.project.mst.module.memory;

import com.project.mst.infra.database.model.Model;
import com.project.mst.module.playsessions.PlaySessionModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "memory_round")
@Entity(name = "memory_round")
@Getter
@Setter
@NoArgsConstructor
public class MemoryRoundModel extends Model {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "play_session_id", nullable = false, unique = true)
    protected PlaySessionModel playSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "theme_id", nullable = false)
    protected CardThemeModel theme;

    @Column(name = "pairs", nullable = false)
    protected Integer pairs;

    @Column(name = "errors", nullable = false)
    protected Integer errors = 0;
}
