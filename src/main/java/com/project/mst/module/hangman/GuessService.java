package com.project.mst.module.hangman;

import com.project.mst.infra.service.CrudService;

import org.springframework.stereotype.Service;

@Service
public class GuessService extends CrudService<GuessModel> {

    public GuessService(GuessRepository repository) {
        super(repository);
    }
}
