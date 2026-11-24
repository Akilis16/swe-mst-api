package com.project.mst.module.game2048.dto;

import java.util.List;
import java.util.UUID;

import com.project.mst.module.game2048.Game2048Result;
import com.project.mst.module.scores.dto.ScoreResponse;

public record Game2048RoundResponse(
    UUID roundId,
    UUID playSessionId,
    List<List<Integer>> board,
    int points,
    int moves,
    int maxTile,
    boolean finished,
    Game2048Result result,
    ScoreResponse score
) {
}
