package com.project.mst.module.quizzes;

import com.project.mst.infra.database.model.Model;
import com.project.mst.module.playsessions.PlaySessionModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "quiz_attempt")
@Entity(name = "quiz_attempt")
@Getter
@Setter
@NoArgsConstructor
public class QuizAttemptModel extends Model {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "play_session_id", nullable = false, unique = true)
    protected PlaySessionModel playSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    protected QuizModel quiz;

    @Column(name = "correct_count", nullable = false)
    protected Integer correctCount = 0;
}
