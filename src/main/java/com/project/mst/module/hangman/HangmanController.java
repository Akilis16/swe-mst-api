package com.project.mst.module.hangman;

import java.util.List;
import java.util.UUID;

import com.project.mst.module.hangman.dto.CategoryResponse;
import com.project.mst.module.hangman.dto.GuessRequest;
import com.project.mst.module.hangman.dto.GuessResponse;
import com.project.mst.module.hangman.dto.HangmanRoundResponse;
import com.project.mst.module.hangman.dto.StartHangmanRequest;

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
@RequestMapping("/hangman")
@Tag(name = "Forca", description = "Categorias, partidas e palpites; a palavra só é revelada ao fim")
public class HangmanController {

    private final HangmanService service;

    public HangmanController(HangmanService service) {
        this.service = service;
    }

    @GetMapping("/categories")
    @Operation(summary = "Categorias com palavras ativas")
    public List<CategoryResponse> categories() {
        return service.categories();
    }

    @PostMapping("/rounds")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Inicia uma partida", description = "Sorteia uma palavra ativa da categoria e devolve só a máscara.")
    public HangmanRoundResponse start(@Valid @RequestBody StartHangmanRequest request) {
        return service.start(request);
    }

    @GetMapping("/rounds/{id}")
    @Operation(summary = "Estado da partida")
    public HangmanRoundResponse find(@PathVariable UUID id) {
        return service.find(id);
    }

    @PostMapping("/rounds/{id}/guesses")
    @Operation(summary = "Tenta uma letra", description = "6 erros = derrota; todas as letras = vitória. Erros: 409 HANGMAN_LETTER_ALREADY_USED, HANGMAN_ROUND_FINISHED; 422 HANGMAN_LETTER_INVALID.")
    public GuessResponse guess(@PathVariable UUID id, @Valid @RequestBody GuessRequest request) {
        return service.guess(id, request);
    }
}
