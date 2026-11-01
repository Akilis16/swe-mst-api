package com.project.mst.module.memory.dto;

import java.util.List;

import com.project.mst.module.scores.dto.ScoreResponse;

public record MemoryMoveResponse(
    CardView card,
    CardView previousCard,
    boolean pairCompleted,
    int errors,
    int matchedPairs,
    int pairs,
    boolean finished,
    List<Integer> revealedPositions,
    ScoreResponse score
) {
}
