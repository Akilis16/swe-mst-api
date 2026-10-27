package com.project.mst.module.playsessions.dto;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import com.project.mst.module.playsessions.PlaySessionModel;
import com.project.mst.module.playsessions.PlaySessionStatus;
import com.project.mst.module.scores.dto.ScoreResponse;

public record PlaySessionResponse(
    UUID id,
    UUID userId,
    UUID gameId,
    String gameSlug,
    PlaySessionStatus status,
    Instant startedAt,
    Instant endedAt,
    String clientMeta,
    String resultSummary,
    ScoreResponse score
) {

    public static PlaySessionResponse from(PlaySessionModel session, ScoreResponse score) {
        return new PlaySessionResponse(
            session.getId(),
            session.getUser().getId(),
            session.getGame().getId(),
            session.getGame().getSlug(),
            session.getStatus() != null ? session.getStatus()
                : (session.isOpen() ? PlaySessionStatus.IN_PROGRESS : PlaySessionStatus.FINISHED),
            toInstant(session.getStarted_at()),
            toInstant(session.getEnded_at()),
            session.getClient_meta(),
            session.getResult_summary(),
            score);
    }

    private static Instant toInstant(Date date) {
        return date == null ? null : date.toInstant();
    }
}
