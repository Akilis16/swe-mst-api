package com.project.mst.module.quizzes;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/quiz-attempts")
public class QuizAttemptController extends CrudController<QuizAttemptModel> {

    public QuizAttemptController(QuizAttemptService service) {
        super(service);
    }
}
