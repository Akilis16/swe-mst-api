package com.project.mst.module.tictactoe;

import com.project.mst.infra.service.CrudService;

import org.springframework.stereotype.Service;

@Service
public class TicTacToeService extends CrudService<TicTacToeModel> {

    public TicTacToeService(TicTacToeRepository repository) {
        super(repository);
    }
}
