package com.project.mst.module.playsessions;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.project.mst.infra.controller.ApiException;
import com.project.mst.infra.security.CurrentUser;
import com.project.mst.module.games.GameModel;
import com.project.mst.module.playsessions.dto.ProgressPoint;
import com.project.mst.module.playsessions.dto.ProgressResponse;
import com.project.mst.module.scores.ScoreModel;
import com.project.mst.module.scores.ScoreRepository;
import com.project.mst.module.scores.dto.ScoreResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgressService {

    static final int DEFAULT_DAYS = 30;
    static final int MAX_DAYS = 366;
    static final ZoneId ZONE = ZoneOffset.UTC;

    private final PlaySessionRepository sessions;
    private final ScoreRepository scores;

    public ProgressService(PlaySessionRepository sessions, ScoreRepository scores) {
        this.sessions = sessions;
        this.scores = scores;
    }

    @Transactional(readOnly = true)
    public ProgressResponse forCurrentUser(String gameSlug, LocalDate from, LocalDate to) {
        UUID userId = CurrentUser.id()
            .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Usuário não autenticado"));

        LocalDate end = to != null ? to : LocalDate.now(ZONE);
        LocalDate start = from != null ? from : end.minusDays(DEFAULT_DAYS - 1);
        if (start.isAfter(end)) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "PROGRESS_PERIOD_INVALID", "from precisa ser anterior ou igual a to");
        }
        if (start.plusDays(MAX_DAYS).isBefore(end)) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "PROGRESS_PERIOD_TOO_LONG", "Período máximo de " + MAX_DAYS + " dias");
        }

        Date startAt = Date.from(start.atStartOfDay(ZONE).toInstant());
        Date endAt = Date.from(end.plusDays(1).atStartOfDay(ZONE).toInstant());
        String slug = GameModel.normalizeSlug(gameSlug);
        List<PlaySessionModel> list = slug == null || slug.isBlank()
            ? sessions.findByUserIdAndPeriod(userId, startAt, endAt)
            : sessions.findByUserIdAndGameSlugAndPeriod(userId, slug, startAt, endAt);

        return new ProgressResponse(start, end, slug == null || slug.isBlank() ? null : slug, series(list));
    }

    List<ProgressPoint> series(List<PlaySessionModel> list) {
        if (list.isEmpty()) {
            return List.of();
        }
        Map<UUID, ScoreModel> byId = scores.findActiveByPlaySessionIds(list.stream().map(PlaySessionModel::getId).toList())
            .stream().collect(Collectors.toMap(s -> s.getPlaySession().getId(), Function.identity(), (a, b) -> a));

        Map<String, Map<LocalDate, List<PlaySessionModel>>> grouped = new LinkedHashMap<>();
        for (PlaySessionModel session : list) {
            grouped.computeIfAbsent(session.getGame().getSlug(), k -> new LinkedHashMap<>())
                .computeIfAbsent(day(session.getStartedAt()), k -> new ArrayList<>())
                .add(session);
        }

        List<ProgressPoint> points = new ArrayList<>();
        grouped.forEach((slug, days) -> days.forEach((day, group) -> points.add(point(slug, day, group, byId))));
        points.sort(Comparator.comparing(ProgressPoint::gameSlug).thenComparing(ProgressPoint::date));
        return Collections.unmodifiableList(points);
    }

    private static ProgressPoint point(String slug, LocalDate day, List<PlaySessionModel> group, Map<UUID, ScoreModel> byId) {
        long finished = 0;
        long abandoned = 0;
        long durationSum = 0;
        long durationCount = 0;
        double scoreSum = 0;
        long scoreCount = 0;
        long errors = 0;
        long correct = 0;

        for (PlaySessionModel session : group) {
            if (session.getStatus() == PlaySessionStatus.FINISHED) {
                finished++;
            } else if (session.getStatus() == PlaySessionStatus.ABANDONED) {
                abandoned++;
            }
            if (session.getEndedAt() != null) {
                durationSum += PlaySessionService.elapsedSeconds(session);
                durationCount++;
            }
            ScoreModel score = byId.get(session.getId());
            if (score != null) {
                scoreSum += score.getValue() == null ? 0 : score.getValue();
                scoreCount++;
                Map<String, Object> breakdown = ScoreResponse.from(score).breakdown();
                errors += asLong(breakdown.get("errors"));
                correct += asLong(breakdown.get("correct"));
            }
        }

        return new ProgressPoint(slug, day, group.size(), finished, abandoned,
            durationCount == 0 ? null : durationSum / durationCount,
            scoreCount == 0 ? null : Math.round(scoreSum / scoreCount * 100.0) / 100.0,
            errors, correct);
    }

    private static LocalDate day(Date date) {
        return date.toInstant().atZone(ZONE).toLocalDate();
    }

    private static long asLong(Object value) {
        return value instanceof Number number ? number.longValue() : 0;
    }
}
