package com.project.mst.module.quizzes;

import java.util.Optional;
import java.util.UUID;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizAttemptRepository extends BaseRepository<QuizAttemptModel> {

    @Query("select a from quiz_attempt a where a.playSession.id = ?1 and a.deleted_at is null")
    Optional<QuizAttemptModel> findActiveByPlaySessionId(UUID playSessionId);
}
