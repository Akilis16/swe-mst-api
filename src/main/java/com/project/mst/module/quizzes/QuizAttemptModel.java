package com.project.mst.module.quizzes;

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

@Table(name="quiz_attempt")
@Entity(name="quiz_attempt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttemptModel extends Model {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    protected UserModel user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    protected QuizModel quiz;

    @Column(name = "started_at")
    protected Date started_at;

    @Column(name = "finished_at")
    protected Date finished_at;

    @Column(name = "score")
    protected Integer score;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    protected String metadata;
}
