package com.project.mst.module.game2048;

import java.util.Optional;
import java.util.UUID;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface Game2048RoundRepository extends BaseRepository<Game2048RoundModel> {

    @Query("select r from game_2048_round r where r.playSession.id = ?1 and r.deleted_at is null")
    Optional<Game2048RoundModel> findActiveByPlaySessionId(UUID playSessionId);
}
