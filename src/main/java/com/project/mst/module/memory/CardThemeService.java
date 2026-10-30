package com.project.mst.module.memory;

import com.project.mst.infra.service.CrudService;

import org.springframework.stereotype.Service;

@Service
public class CardThemeService extends CrudService<CardThemeModel> {

    public CardThemeService(CardThemeRepository repository) {
        super(repository);
    }
}
