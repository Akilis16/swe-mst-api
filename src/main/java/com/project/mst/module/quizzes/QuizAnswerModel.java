package com.project.mst.module.quizzes;

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

@Table(name="quiz_answer")
@Entity(name="quiz_answer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "free_text_answer", columnDefinition = "text")
    protected String free_text_answer;

    @Column(name = "correct")
    protected Boolean correct;

    @Column(name = "time_taken_ms")
    protected Integer time_taken_ms;
}
