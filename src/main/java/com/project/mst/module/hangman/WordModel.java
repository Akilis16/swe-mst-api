package com.project.mst.module.hangman;

import com.project.mst.infra.database.model.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name="word")
@Entity(name="word")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WordModel extends Model {
    @Column(name = "word", unique = true, nullable = false)
    protected String word;

    @Column(name = "category")
    protected String category;

    @Column(name = "difficulty")
    protected String difficulty;
}
