package com.project.mst.module.playsessions;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/play-sessions")
public class PlaySessionController extends CrudController<PlaySessionModel> {

    public PlaySessionController(PlaySessionService service) {
        super(service);
    }
}
