package com.project.mst.module.quizzes;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/admin/quizzes")
@Tag(name = "Conteúdo (ADMIN)")
public class QuizController extends CrudController<QuizModel> {

    public QuizController(QuizService service) {
        super(service);
    }
}
