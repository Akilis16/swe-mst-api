package com.project.mst.module.quizzes;

import java.util.List;
import java.util.UUID;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizAnswerRepository extends BaseRepository<QuizAnswerModel> {

    @Query("select a from quiz_answer a where a.attempt.id = ?1 and a.deleted_at is null order by a.answeredAt")
    List<QuizAnswerModel> findByAttemptId(UUID attemptId);
}
