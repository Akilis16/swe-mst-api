package com.project.mst.module.game2048;

import java.util.UUID;

import com.project.mst.module.game2048.dto.Game2048MoveRequest;
import com.project.mst.module.game2048.dto.Game2048MoveResponse;
import com.project.mst.module.game2048.dto.Game2048RoundResponse;
import com.project.mst.module.game2048.dto.StartGame2048Request;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/2048/rounds")
@Tag(name = "2048", description = "Partidas com tabuleiro, fusões, peça sorteada e pontos no servidor")
public class Game2048Controller {

    private final Game2048Service service;

    public Game2048Controller(Game2048Service service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Inicia uma partida", description = "Abre a sessão de jogo e devolve o tabuleiro 4x4 com duas peças 2 sorteadas.")
    public Game2048RoundResponse start(@Valid @RequestBody(required = false) StartGame2048Request request) {
        return service.start(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Estado atual da partida")
    public Game2048RoundResponse find(@PathVariable UUID id) {
        return service.find(id);
    }

    @PostMapping("/{id}/moves")
    @Operation(summary = "Desliza as peças", description = "direction: LEFT, RIGHT, UP ou DOWN. Jogada que não mexe o tabuleiro devolve moved=false sem sortear peça. "
        + "Chegar a 2048 = WIN; sem jogadas possíveis = LOSS; nos dois casos a sessão é encerrada e pontuada. Erros: 409 GAME2048_ROUND_FINISHED.")
    public Game2048MoveResponse move(@PathVariable UUID id, @Valid @RequestBody Game2048MoveRequest request) {
        return service.move(id, request);
    }
}
