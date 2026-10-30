package com.project.mst.module.quizzes;

import com.project.mst.infra.database.model.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "option")
@Entity(name = "option")
@Getter
@Setter
@NoArgsConstructor
public class OptionModel extends Model {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    protected QuestionModel question;

    @NotBlank
    @Column(name = "text", columnDefinition = "text", nullable = false)
    protected String text;

    @NotNull
    @Column(name = "is_correct", nullable = false)
    protected Boolean isCorrect = false;
}
