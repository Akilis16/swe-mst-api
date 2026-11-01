package com.project.mst.module.memory;

import java.util.List;
import java.util.UUID;

import com.project.mst.module.memory.dto.MemoryMoveRequest;
import com.project.mst.module.memory.dto.MemoryMoveResponse;
import com.project.mst.module.memory.dto.MemoryRoundResponse;
import com.project.mst.module.memory.dto.StartRoundRequest;
import com.project.mst.module.memory.dto.ThemeResponse;

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
@RequestMapping("/memory")
@Tag(name = "Jogo da Memória", description = "Temas, partidas e viradas de carta; o par é decidido no servidor")
public class MemoryController {

    private final MemoryService service;

    public MemoryController(MemoryService service) {
        this.service = service;
    }

    @GetMapping("/themes")
    @Operation(summary = "Temas ativos")
    public List<ThemeResponse> themes() {
        return service.themes();
    }

    @PostMapping("/rounds")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Inicia uma partida", description = "18 posições embaralhadas no servidor, sem identificar a carta.")
    public MemoryRoundResponse start(@Valid @RequestBody StartRoundRequest request) {
        return service.start(request);
    }

    @GetMapping("/rounds/{id}")
    @Operation(summary = "Estado atual da partida", description = "Só as cartas já fixadas vêm identificadas.")
    public MemoryRoundResponse find(@PathVariable UUID id) {
        return service.find(id);
    }

    @PostMapping("/rounds/{id}/moves")
    @Operation(summary = "Vira uma carta", description = "A segunda carta de cada tentativa fecha o par ou conta um erro. Erros: 409 MEMORY_POSITION_ALREADY_MATCHED, MEMORY_POSITION_ALREADY_REVEALED, MEMORY_ROUND_FINISHED; 422 MEMORY_POSITION_INVALID.")
    public MemoryMoveResponse move(@PathVariable UUID id, @Valid @RequestBody MemoryMoveRequest request) {
        return service.move(id, request);
    }
}
