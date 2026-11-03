package com.project.mst.module.playsessions.dto;

import java.time.LocalDate;

public record ProgressPoint(
    String gameSlug,
    LocalDate date,
    long sessions,
    long finished,
    long abandoned,
    Long averageDurationSeconds,
    Double averageScore,
    long errors,
    long correct
) {
}
