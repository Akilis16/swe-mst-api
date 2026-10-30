package com.project.mst.module.quizzes;

import com.project.mst.infra.database.model.Model;
import com.project.mst.module.games.GameModel;

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

@Table(name = "quiz")
@Entity(name = "quiz")
@Getter
@Setter
@NoArgsConstructor
public class QuizModel extends Model {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    protected GameModel game;

    @NotBlank
    @Size(max = 100)
    @Column(name = "title", nullable = false, length = 100)
    protected String title;

    @Column(name = "active", nullable = false)
    protected boolean active = true;
}
