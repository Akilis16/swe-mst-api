package com.project.mst.module.hangman;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/admin/words")
@Tag(name = "Conteúdo (ADMIN)")
public class WordController extends CrudController<WordModel> {

    public WordController(WordService service) {
        super(service);
    }
}
