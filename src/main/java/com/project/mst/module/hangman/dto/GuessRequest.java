package com.project.mst.module.hangman.dto;

import jakarta.validation.constraints.NotBlank;

public record GuessRequest(@NotBlank String letter) {
}
