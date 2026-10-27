package com.project.mst.module.scores.dto;

import java.time.Instant;
import java.util.UUID;

import com.project.mst.module.scores.ScoreModel;

public record ScoreResponse(
    UUID id,
    UUID userId,
    UUID gameId,
    UUID playSessionId,
    Integer value,
    String rankContext,
    Instant createdAt
) {

    public static ScoreResponse from(ScoreModel score) {
        return new ScoreResponse(
            score.getId(),
            score.getUser().getId(),
            score.getGame().getId(),
            score.getPlay_session().getId(),
            score.getScore_value(),
            score.getRank_context(),
            score.getCreated_at() == null ? null : score.getCreated_at().toInstant());
    }
}
