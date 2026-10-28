package com.project.mst.module.games.dto;

import jakarta.validation.constraints.NotNull;

public record ActiveRequest(@NotNull Boolean active) {
}
