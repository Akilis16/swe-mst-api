package com.project.mst.module.games;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/games")
public class GameController extends CrudController<GameModel> {

    public GameController(GameService service) {
        super(service);
    }
}
