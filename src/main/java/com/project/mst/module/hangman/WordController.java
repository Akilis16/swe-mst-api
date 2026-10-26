package com.project.mst.module.hangman;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/words")
public class WordController extends CrudController<WordModel> {

    public WordController(WordService service) {
        super(service);
    }
}
