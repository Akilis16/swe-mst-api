package com.project.mst.module.playsessions;

import java.util.List;
import java.util.UUID;

import com.project.mst.module.playsessions.dto.FinishSessionRequest;
import com.project.mst.module.playsessions.dto.PlaySessionResponse;
import com.project.mst.module.playsessions.dto.StartSessionRequest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public List<PlaySessionResponse> findAll() {
        return service.findVisible();
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
    public PlaySessionResponse finish(@PathVariable UUID id, @RequestBody(required = false) FinishSessionRequest request) {
        return service.finish(id, request);
    }

    @PostMapping("/{id}/abandon")
    public PlaySessionResponse abandon(@PathVariable UUID id) {
        return service.abandon(id);
    }
}
