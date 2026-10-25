package com.project.mst.module.hangman;

import com.project.mst.infra.service.CrudService;

import org.springframework.stereotype.Service;

@Service
public class HangmanSessionService extends CrudService<HangmanSessionModel> {

    public HangmanSessionService(HangmanSessionRepository repository) {
        super(repository);
    }
}
