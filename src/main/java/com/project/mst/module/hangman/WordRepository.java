package com.project.mst.module.hangman;

import java.util.Optional;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface WordRepository extends BaseRepository<WordModel> {
    Optional<WordModel> findByWord(String word);
}
