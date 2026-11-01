package com.project.mst.module.sudoku;

import java.util.UUID;

import com.project.mst.module.sudoku.dto.EntryRequest;
import com.project.mst.module.sudoku.dto.EntryResponse;
import com.project.mst.module.sudoku.dto.RevealResponse;
import com.project.mst.module.sudoku.dto.StartSudokuRequest;
import com.project.mst.module.sudoku.dto.SudokuAttemptResponse;

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
@RequestMapping("/sudoku/attempts")
@Tag(name = "Sudoku", description = "Tentativas com puzzle sorteado, conferência de cada casa, chances e tempo no servidor")
public class SudokuController {

    private final SudokuService service;

    public SudokuController(SudokuService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Inicia uma tentativa", description = "Sorteia um puzzle ativo do nível. timeLimitSeconds: 180, 300, 600 ou nulo (sem limite). A resposta traz só as casas dadas, nunca a solução.")
    public SudokuAttemptResponse start(@Valid @RequestBody StartSudokuRequest request) {
        return service.start(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Estado da tentativa", description = "Grade atual por linha, 0 nas casas vazias.")
    public SudokuAttemptResponse find(@PathVariable UUID id) {
        return service.find(id);
    }

    @PostMapping("/{id}/entries")
    @Operation(summary = "Preenche ou apaga uma casa", description = "Valor errado conta um erro e é apagado; 3 erros encerram com LOSS; 81 casas corretas encerram com WIN. Erros: 409 SUDOKU_CELL_IS_GIVEN, SUDOKU_ATTEMPT_FINISHED.")
    public EntryResponse entry(@PathVariable UUID id, @Valid @RequestBody EntryRequest request) {
        return service.entry(id, request);
    }

    @PostMapping("/{id}/reveal")
    @Operation(summary = "Revela a solução", description = "Encerra a sessão como abandonada, sem pontuação. 409 SUDOKU_ATTEMPT_FINISHED.")
    public RevealResponse reveal(@PathVariable UUID id) {
        return service.reveal(id);
    }
}
