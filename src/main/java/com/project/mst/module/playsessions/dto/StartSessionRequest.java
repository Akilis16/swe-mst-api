package com.project.mst.module.playsessions.dto;

import com.project.mst.module.playsessions.Difficulty;

import jakarta.validation.constraints.NotBlank;

public record StartSessionRequest(@NotBlank String gameSlug, Difficulty difficulty, Boolean timed, String clientMeta) {
}
