package com.project.mst.module.tictactoe;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tic-tac-toe-moves")
public class TicTacToeMoveController extends CrudController<TicTacToeMoveModel> {

    public TicTacToeMoveController(TicTacToeMoveService service) {
        super(service);
    }
}
