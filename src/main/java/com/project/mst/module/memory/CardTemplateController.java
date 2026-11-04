package com.project.mst.module.memory;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/admin/card-templates")
@Tag(name = "Conteúdo (ADMIN)")
public class CardTemplateController extends CrudController<CardTemplateModel> {

    public CardTemplateController(CardTemplateService service) {
        super(service);
    }
}
