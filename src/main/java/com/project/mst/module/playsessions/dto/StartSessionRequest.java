package com.project.mst.module.playsessions.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record StartSessionRequest(@NotNull UUID gameId, String clientMeta) {
}
