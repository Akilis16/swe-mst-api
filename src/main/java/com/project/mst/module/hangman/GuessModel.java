package com.project.mst.module.hangman;

import com.project.mst.infra.database.model.Model;

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

@Table(name="guess")
@Entity(name="guess")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GuessModel extends Model {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    protected HangmanSessionModel session;

    @Column(name = "letter", length = 1, nullable = false)
    protected String letter;

    @Column(name = "correct")
    protected Boolean correct;
}
