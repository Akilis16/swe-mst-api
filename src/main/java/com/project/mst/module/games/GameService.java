package com.project.mst.module.games;

import com.project.mst.infra.service.CrudService;

import org.springframework.stereotype.Service;

@Service
public class GameService extends CrudService<GameModel> {

    public GameService(GameRepository repository) {
        super(repository);
    }
}
