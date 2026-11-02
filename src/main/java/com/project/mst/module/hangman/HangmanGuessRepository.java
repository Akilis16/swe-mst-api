package com.project.mst.module.hangman;

import java.util.List;
import java.util.UUID;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HangmanGuessRepository extends BaseRepository<HangmanGuessModel> {

    @Query("select g from hangman_guess g where g.round.id = ?1 and g.deleted_at is null order by g.created_at, g.letter")
    List<HangmanGuessModel> findByRoundIdOrdered(UUID roundId);
}
