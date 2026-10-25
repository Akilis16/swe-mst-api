package com.project.mst.module.tictactoe;

import com.project.mst.infra.service.CrudService;

import org.springframework.stereotype.Service;

@Service
public class TicTacToeMoveService extends CrudService<TicTacToeMoveModel> {

    public TicTacToeMoveService(TicTacToeMoveRepository repository) {
        super(repository);
    }
}
