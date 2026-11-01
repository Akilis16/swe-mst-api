package com.project.mst.module.memory.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record StartRoundRequest(@NotNull UUID themeId, Boolean timed) {
}
