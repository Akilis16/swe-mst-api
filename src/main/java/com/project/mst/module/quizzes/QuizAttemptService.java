package com.project.mst.module.quizzes;

import com.project.mst.infra.service.CrudService;

import org.springframework.stereotype.Service;

@Service
public class QuizAttemptService extends CrudService<QuizAttemptModel> {

    public QuizAttemptService(QuizAttemptRepository repository) {
        super(repository);
    }
}
