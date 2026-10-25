package com.project.mst.module.hangman;

import com.project.mst.infra.service.CrudService;

import org.springframework.stereotype.Service;

@Service
public class WordService extends CrudService<WordModel> {

    public WordService(WordRepository repository) {
        super(repository);
    }
}
