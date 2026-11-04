package com.project.mst.module.games;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.project.mst.module.games.dto.ActiveRequest;
import com.project.mst.module.games.dto.GameResponse;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/games")
@Tag(name = "Catálogo", description = "Jogos ativos; escrita restrita a ADMIN")
public class GameController {

    private final GameService service;

    public GameController(GameService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Catálogo de jogos ativos", description = "ADMIN pode passar includeInactive=true.")
    public List<GameResponse> catalog(@RequestParam(defaultValue = "false") boolean includeInactive) {
        return service.catalog(includeInactive);
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Jogo pelo slug")
    public GameResponse findBySlug(@PathVariable String slug) {
        return service.findBySlug(slug);
    }

    @PostMapping
    public ResponseEntity<GameResponse> create(@Valid @RequestBody GameModel game) {
        GameModel created = service.create(game);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{slug}").buildAndExpand(created.getSlug()).toUri();
        return ResponseEntity.created(location).body(GameResponse.from(created));
    }

    @PutMapping("/{id}")
    public GameResponse update(@PathVariable UUID id, @Valid @RequestBody GameModel game) {
        return GameResponse.from(service.update(id, game));
    }

    @PatchMapping("/{id}/active")
    @Operation(summary = "Ativa ou desativa um jogo (ADMIN)")
    public GameResponse setActive(@PathVariable UUID id, @Valid @RequestBody ActiveRequest request) {
        return service.setActive(id, request.active());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
