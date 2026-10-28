package com.project.mst.module.hangman;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/guesses")
public class GuessController extends CrudController<GuessModel> {

    public GuessController(GuessService service) {
        super(service);
    }
}
