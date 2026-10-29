package com.project.mst.module.playsessions;

import java.util.UUID;

import com.project.mst.module.playsessions.dto.PlaySessionResponse;
import com.project.mst.module.playsessions.dto.StartSessionRequest;

import jakarta.validation.Valid;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/play-sessions")
public class PlaySessionController {

    private final PlaySessionService service;

    public PlaySessionController(PlaySessionService service) {
        this.service = service;
    }

    @GetMapping
    public PagedModel<PlaySessionResponse> findAll(
            @RequestParam(required = false) String game,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return new PagedModel<>(service.pageAll(game, Paging.of(page, size)));
    }

    @GetMapping("/{id}")
    public PlaySessionResponse findById(@PathVariable UUID id) {
        return service.findVisibleById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaySessionResponse start(@Valid @RequestBody StartSessionRequest request) {
        return service.start(request);
    }

    @PostMapping("/{id}/finish")
    public PlaySessionResponse finish(@PathVariable UUID id) {
        return service.finish(id);
    }

    @PostMapping("/{id}/abandon")
    public PlaySessionResponse abandon(@PathVariable UUID id) {
        return service.abandon(id);
    }
}
