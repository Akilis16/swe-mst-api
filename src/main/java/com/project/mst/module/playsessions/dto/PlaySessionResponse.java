package com.project.mst.module.playsessions.dto;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import com.project.mst.module.playsessions.Difficulty;
import com.project.mst.module.playsessions.PlaySessionModel;
import com.project.mst.module.playsessions.PlaySessionStatus;
import com.project.mst.module.scores.dto.ScoreResponse;

public record PlaySessionResponse(
    UUID id,
    UUID userId,
    UUID gameId,
    String gameSlug,
    PlaySessionStatus status,
    Difficulty difficulty,
    boolean timed,
    Instant startedAt,
    Instant endedAt,
    String clientMeta,
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
            session.getDifficulty(),
            session.getTimed() == null || session.getTimed(),
            toInstant(session.getStartedAt()),
            toInstant(session.getEndedAt()),
            session.getClientMeta(),
            score);
    }

    private static Instant toInstant(Date date) {
        return date == null ? null : date.toInstant();
    }
}
