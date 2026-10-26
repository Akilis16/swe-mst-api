package com.project.mst.module.quizzes;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/questions")
public class QuestionController extends CrudController<QuestionModel> {

    public QuestionController(QuestionService service) {
        super(service);
    }
}
