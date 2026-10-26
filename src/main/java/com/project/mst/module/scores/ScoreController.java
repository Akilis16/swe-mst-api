package com.project.mst.module.scores;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scores")
public class ScoreController extends CrudController<ScoreModel> {

    public ScoreController(ScoreService service) {
        super(service);
    }
}
