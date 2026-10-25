package com.project.mst.module.hangman;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface HangmanSessionRepository extends BaseRepository<HangmanSessionModel> {}
