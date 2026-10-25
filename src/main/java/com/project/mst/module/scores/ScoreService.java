package com.project.mst.module.scores;

import com.project.mst.infra.service.CrudService;

import org.springframework.stereotype.Service;

@Service
public class ScoreService extends CrudService<ScoreModel> {

    public ScoreService(ScoreRepository repository) {
        super(repository);
    }
}
