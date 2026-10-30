package com.project.mst.module.hangman;

import com.project.mst.infra.database.model.Model;
import com.project.mst.module.playsessions.Difficulty;

import java.util.Locale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "word")
@Entity(name = "word")
@Getter
@Setter
@NoArgsConstructor
public class WordModel extends Model {

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[A-Z]+$", message = "apenas letras de A a Z, sem acentos")
    @Column(name = "text", nullable = false, length = 50)
    protected String text;

    @NotBlank
    @Size(max = 50)
    @Column(name = "category", nullable = false, length = 50)
    protected String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", length = 20)
    protected Difficulty difficulty;

    @Column(name = "active", nullable = false)
    protected boolean active = true;

    public void setText(String text) {
        this.text = text == null ? null : text.trim().toUpperCase(Locale.ROOT);
    }

    public void setCategory(String category) {
        this.category = category == null ? null : category.trim().toUpperCase(Locale.ROOT);
    }
}
