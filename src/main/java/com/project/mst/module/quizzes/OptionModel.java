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

@Table(name="option")
@Entity(name="option")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OptionModel extends Model {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    protected QuestionModel question;

    @Column(name = "text", columnDefinition = "text", nullable = false)
    protected String text;

    @Column(name = "is_correct", nullable = false)
    protected Boolean is_correct;
}
