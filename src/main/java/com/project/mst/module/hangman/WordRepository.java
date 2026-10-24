package com.project.mst.module.hangman;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WordRepository extends JpaRepository<WordModel, UUID> {
    Optional<WordModel> findByWord(String word);
}
