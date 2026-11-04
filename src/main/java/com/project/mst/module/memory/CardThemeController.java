package com.project.mst.module.memory;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/admin/card-themes")
@Tag(name = "Conteúdo (ADMIN)", description = "Temas e cartas da memória, quizzes, perguntas, alternativas, puzzles e palavras")
public class CardThemeController extends CrudController<CardThemeModel> {

    public CardThemeController(CardThemeService service) {
        super(service);
    }
}
