package com.project.mst.module.quizzes;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface QuizAttemptRepository extends BaseRepository<QuizAttemptModel> {}
