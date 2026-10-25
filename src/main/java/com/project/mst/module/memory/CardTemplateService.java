package com.project.mst.module.memory;

import com.project.mst.infra.service.CrudService;

import org.springframework.stereotype.Service;

@Service
public class CardTemplateService extends CrudService<CardTemplateModel> {

    public CardTemplateService(CardTemplateRepository repository) {
        super(repository);
    }
}
