package com.project.mst.module.tictactoe;

import java.util.UUID;

import com.project.mst.module.tictactoe.dto.MoveRequest;
import com.project.mst.module.tictactoe.dto.StartMatchRequest;
import com.project.mst.module.tictactoe.dto.TicTacToeMatchResponse;

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
@RequestMapping("/tic-tac-toe/matches")
@Tag(name = "Jogo da Velha", description = "Partidas com regras, vez e jogada do computador no servidor")
public class TicTacToeController {

    private final TicTacToeService service;

    public TicTacToeController(TicTacToeService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Inicia uma partida", description = "Abre a sessão de jogo e devolve o tabuleiro vazio. X sempre começa; no modo VS_COMPUTER o jogador é X.")
    public TicTacToeMatchResponse start(@Valid @RequestBody StartMatchRequest request) {
        return service.start(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Estado atual da partida")
    public TicTacToeMatchResponse find(@PathVariable UUID id) {
        return service.find(id);
    }

    @PostMapping("/{id}/moves")
    @Operation(summary = "Registra uma jogada", description = "Erros: 409 TICTACTOE_CELL_OCCUPIED, TICTACTOE_NOT_YOUR_TURN, TICTACTOE_MATCH_FINISHED; 422 fora do tabuleiro. No modo VS_COMPUTER a resposta já inclui a jogada do servidor.")
    public TicTacToeMatchResponse move(@PathVariable UUID id, @Valid @RequestBody MoveRequest request) {
        return service.move(id, request);
    }
}
