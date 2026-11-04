package com.project.mst.module.playsessions;

import com.project.mst.module.playsessions.dto.PlaySessionResponse;

import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/me/play-sessions")
@Tag(name = "Sessões", description = "Histórico paginado do próprio usuário")
public class MePlaySessionController {

    private final PlaySessionService service;

    public MePlaySessionController(PlaySessionService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Histórico paginado do próprio usuário", description = "Filtro opcional por game (slug); cada sessão vem com a pontuação.")
    public PagedModel<PlaySessionResponse> mySessions(
            @RequestParam(required = false) String game,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return new PagedModel<>(service.pageForCurrentUser(game, Paging.of(page, size)));
    }
}
