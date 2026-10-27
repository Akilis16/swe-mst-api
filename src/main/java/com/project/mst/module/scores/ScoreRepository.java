package com.project.mst.module.scores;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoreRepository extends BaseRepository<ScoreModel> {

    @Query("select s from score s where s.user.id = ?1 and s.deleted_at is null order by s.created_at desc")
    List<ScoreModel> findAllActiveByUserId(UUID userId);

    @Query("select s from score s where s.play_session.id = ?1 and s.deleted_at is null")
    Optional<ScoreModel> findActiveByPlaySessionId(UUID playSessionId);
}
