package com.project.mst.module.hangman;

import com.project.mst.infra.database.model.Model;
import com.project.mst.module.playsessions.PlaySessionModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "hangman_round")
@Entity(name = "hangman_round")
@Getter
@Setter
@NoArgsConstructor
public class HangmanRoundModel extends Model {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "play_session_id", nullable = false, unique = true)
    protected PlaySessionModel playSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "word_id", nullable = false)
    protected WordModel word;

    @Column(name = "max_errors", nullable = false)
    protected Integer maxErrors = 6;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 10)
    protected HangmanResult result;
}
