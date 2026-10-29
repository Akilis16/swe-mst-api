package com.project.mst.module.scores;

import java.util.Optional;
import java.util.UUID;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoreRepository extends BaseRepository<ScoreModel> {

    @Query("select s from score s where s.playSession.id = ?1 and s.deleted_at is null")
    Optional<ScoreModel> findActiveByPlaySessionId(UUID playSessionId);
}
