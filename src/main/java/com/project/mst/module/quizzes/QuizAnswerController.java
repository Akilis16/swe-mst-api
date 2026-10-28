package com.project.mst.module.quizzes;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/quiz-answers")
public class QuizAnswerController extends CrudController<QuizAnswerModel> {

    public QuizAnswerController(QuizAnswerService service) {
        super(service);
    }
}
