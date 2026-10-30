package com.project.mst.module.hangman;

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

@Table(name = "hangman_guess")
@Entity(name = "hangman_guess")
@Getter
@Setter
@NoArgsConstructor
public class HangmanGuessModel extends Model {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "round_id", nullable = false)
    protected HangmanRoundModel round;

    @Column(name = "letter", nullable = false, length = 1)
    protected String letter;

    @Column(name = "correct", nullable = false)
    protected boolean correct;
}
