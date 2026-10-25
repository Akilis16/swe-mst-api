package com.project.mst.module.quizzes;

import com.project.mst.infra.service.CrudService;

import org.springframework.stereotype.Service;

@Service
public class QuizService extends CrudService<QuizModel> {

    public QuizService(QuizRepository repository) {
        super(repository);
    }
}
