package com.project.mst.module.quizzes;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/quizzes")
public class QuizController extends CrudController<QuizModel> {

    public QuizController(QuizService service) {
        super(service);
    }
}
