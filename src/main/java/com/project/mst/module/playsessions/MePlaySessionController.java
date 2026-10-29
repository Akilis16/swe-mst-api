package com.project.mst.module.playsessions;

import com.project.mst.module.playsessions.dto.PlaySessionResponse;

import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me/play-sessions")
public class MePlaySessionController {

    private final PlaySessionService service;

    public MePlaySessionController(PlaySessionService service) {
        this.service = service;
    }

    @GetMapping
    public PagedModel<PlaySessionResponse> mySessions(
            @RequestParam(required = false) String game,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return new PagedModel<>(service.pageForCurrentUser(game, Paging.of(page, size)));
    }
}
