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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/play-sessions")
@Tag(name = "Sessões", description = "Sessão de jogo genérica, encerramento e abandono")
public class PlaySessionController {

    private final PlaySessionService service;

    public PlaySessionController(PlaySessionService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Todas as sessões (ADMIN)")
    public PagedModel<PlaySessionResponse> findAll(
            @RequestParam(required = false) String game,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return new PagedModel<>(service.pageAll(game, Paging.of(page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Uma sessão do próprio usuário ou qualquer uma para ADMIN")
    public PlaySessionResponse findById(@PathVariable UUID id) {
        return service.findVisibleById(id);
    }

    @PostMapping
    @Operation(summary = "Abre uma sessão genérica", description = "Os jogos têm endpoints próprios de início que criam a sessão; este existe para jogos sem endpoint específico.")
    @ResponseStatus(HttpStatus.CREATED)
    public PlaySessionResponse start(@Valid @RequestBody StartSessionRequest request) {
        return service.start(request);
    }

    @PostMapping("/{id}/finish")
    @Operation(summary = "Encerra a sessão e pontua", description = "Idempotente. 409 SESSION_ABANDONED.")
    public PlaySessionResponse finish(@PathVariable UUID id) {
        return service.finish(id);
    }

    @PostMapping("/{id}/abandon")
    @Operation(summary = "Abandona a sessão", description = "409 SESSION_ALREADY_ENDED.")
    public PlaySessionResponse abandon(@PathVariable UUID id) {
        return service.abandon(id);
    }
}
