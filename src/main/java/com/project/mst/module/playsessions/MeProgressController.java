package com.project.mst.module.playsessions;

import java.time.LocalDate;

import com.project.mst.module.playsessions.dto.ProgressResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me/progress")
@Tag(name = "Progresso", description = "Histórico agregado do próprio usuário por jogo e por dia")
public class MeProgressController {

    private final ProgressService service;

    public MeProgressController(ProgressService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Séries de progresso por jogo e dia",
        description = "Partidas, concluídas, abandonadas, duração média, pontuação média, erros e acertos por dia. Período padrão: últimos 30 dias (máximo 366).")
    public ProgressResponse progress(
            @RequestParam(required = false) String game,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.forCurrentUser(game, from, to);
    }
}
