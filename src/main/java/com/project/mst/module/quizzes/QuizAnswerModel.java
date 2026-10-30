package com.project.mst.module.quizzes;

import com.project.mst.infra.database.model.Model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "quiz_answer")
@Entity(name = "quiz_answer")
@Getter
@Setter
@NoArgsConstructor
public class QuizAnswerModel extends Model {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    protected QuizAttemptModel attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    protected QuestionModel question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    protected OptionModel option;

    @Column(name = "correct", nullable = false)
    protected boolean correct;

    @Column(name = "answered_at", nullable = false)
    protected Date answeredAt;
}
