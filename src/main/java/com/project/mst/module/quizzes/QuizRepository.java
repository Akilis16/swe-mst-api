package com.project.mst.module.quizzes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizRepository extends BaseRepository<QuizModel> {

    @Query("select q from quiz q where q.deleted_at is null and q.active = true and q.game.slug = ?1 order by q.created_at, q.title")
    List<QuizModel> findEnabledByGameSlug(String gameSlug);

    @Query("select q from quiz q where q.id = ?1 and q.deleted_at is null and q.active = true")
    Optional<QuizModel> findEnabledById(UUID id);
}
