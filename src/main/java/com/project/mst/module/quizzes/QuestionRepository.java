package com.project.mst.module.quizzes;

import java.util.List;
import java.util.UUID;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends BaseRepository<QuestionModel> {

    @Query("select q from question q where q.quiz.id = ?1 and q.deleted_at is null and q.active = true order by q.position, q.created_at, q.id")
    List<QuestionModel> findEnabledByQuizIdOrdered(UUID quizId);
}
