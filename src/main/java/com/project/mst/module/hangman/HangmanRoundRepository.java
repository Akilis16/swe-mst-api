package com.project.mst.module.hangman;

import java.util.Optional;
import java.util.UUID;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HangmanRoundRepository extends BaseRepository<HangmanRoundModel> {

    @Query("select r from hangman_round r where r.playSession.id = ?1 and r.deleted_at is null")
    Optional<HangmanRoundModel> findActiveByPlaySessionId(UUID playSessionId);
}
