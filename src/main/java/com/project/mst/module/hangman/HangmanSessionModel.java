package com.project.mst.module.hangman;

import com.project.mst.infra.database.model.Model;
import com.project.mst.module.users.UserModel;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Table(name="hangman_session")
@Entity(name="hangman_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HangmanSessionModel extends Model {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    protected UserModel user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "word_id", nullable = false)
    protected WordModel word;

    @Column(name = "started_at")
    protected Date started_at;

    @Column(name = "ended_at")
    protected Date ended_at;

    @Column(name = "remaining_attempts")
    protected Integer remaining_attempts;

    @Column(name = "status")
    protected String status;

    @Column(name = "masked_word")
    protected String masked_word;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "guesses_summary", columnDefinition = "jsonb")
    protected String guesses_summary;
}
