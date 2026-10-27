package com.project.mst.module.scores;

import java.util.List;
import java.util.UUID;

import com.project.mst.module.scores.dto.ScoreResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scores")
public class ScoreController {

    private final ScoreService service;

    public ScoreController(ScoreService service) {
        this.service = service;
    }

    @GetMapping
    public List<ScoreResponse> findAll() {
        return service.findVisible();
    }

    @GetMapping("/{id}")
    public ScoreResponse findById(@PathVariable UUID id) {
        return service.findVisibleById(id);
    }
}
