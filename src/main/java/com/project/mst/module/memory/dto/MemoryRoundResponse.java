package com.project.mst.module.memory.dto;

import java.util.List;
import java.util.UUID;

import com.project.mst.module.scores.dto.ScoreResponse;

public record MemoryRoundResponse(
    UUID roundId,
    UUID playSessionId,
    UUID themeId,
    String backImagePath,
    int pairs,
    int matchedPairs,
    int errors,
    boolean finished,
    List<CardView> cards,
    List<Integer> revealedPositions,
    ScoreResponse score
) {
}
