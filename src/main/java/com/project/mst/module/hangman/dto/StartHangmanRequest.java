package com.project.mst.module.hangman.dto;

import jakarta.validation.constraints.NotBlank;

public record StartHangmanRequest(@NotBlank String category, Boolean timed) {
}
