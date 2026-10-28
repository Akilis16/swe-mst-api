package com.project.mst.module.memory;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/card-templates")
public class CardTemplateController extends CrudController<CardTemplateModel> {

    public CardTemplateController(CardTemplateService service) {
        super(service);
    }
}
