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
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "question")
@Entity(name = "question")
@Getter
@Setter
@NoArgsConstructor
public class QuestionModel extends Model {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    protected QuizModel quiz;

    @Size(max = 50)
    @Column(name = "category", length = 50)
    protected String category;

    @NotBlank
    @Column(name = "text", columnDefinition = "text", nullable = false)
    protected String text;

    @Column(name = "active", nullable = false)
    protected boolean active = true;
}
