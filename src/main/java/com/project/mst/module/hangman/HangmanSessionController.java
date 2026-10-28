package com.project.mst.module.hangman;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hangman-sessions")
public class HangmanSessionController extends CrudController<HangmanSessionModel> {

    public HangmanSessionController(HangmanSessionService service) {
        super(service);
    }
}
