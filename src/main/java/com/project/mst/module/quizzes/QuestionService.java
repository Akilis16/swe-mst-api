package com.project.mst.module.quizzes;

import com.project.mst.infra.service.CrudService;

import org.springframework.stereotype.Service;

@Service
public class QuestionService extends CrudService<QuestionModel> {

    public QuestionService(QuestionRepository repository) {
        super(repository);
    }
}
