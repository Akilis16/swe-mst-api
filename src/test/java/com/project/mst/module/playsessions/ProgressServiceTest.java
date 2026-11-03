package com.project.mst.module.playsessions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.project.mst.infra.controller.ApiException;
import com.project.mst.infra.security.Roles;
import com.project.mst.module.games.GameModel;
import com.project.mst.module.playsessions.dto.ProgressPoint;
import com.project.mst.module.playsessions.dto.ProgressResponse;
import com.project.mst.module.scores.ScoreModel;
import com.project.mst.module.scores.ScoreRepository;
import com.project.mst.module.users.UserModel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock PlaySessionRepository sessions;
    @Mock ScoreRepository scores;

    ProgressService service;
    UUID userId = UUID.randomUUID();
    UserModel user = new UserModel();
    GameModel velha = new GameModel();
    GameModel forca = new GameModel();

    @BeforeEach
    void setUp() {
        service = new ProgressService(sessions, scores);
        user.setId(userId);
        velha.setSlug("velha");
        forca.setSlug("forca");
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "HS256").subject(userId.toString()).build();
        SecurityContextHolder.getContext().setAuthentication(
            new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_" + Roles.PLAYER))));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void agrupaPorJogoEDiaComMediasErrosEAcertos() {
        Date day1 = Date.from(LocalDate.of(2026, 11, 1).atStartOfDay(ProgressService.ZONE).toInstant());
        Date day2 = Date.from(LocalDate.of(2026, 11, 2).atStartOfDay(ProgressService.ZONE).toInstant());
        PlaySessionModel a = session(velha, day1, 30, PlaySessionStatus.FINISHED);
        PlaySessionModel b = session(velha, day1, 50, PlaySessionStatus.FINISHED);
        PlaySessionModel c = session(velha, day2, 0, PlaySessionStatus.ABANDONED);
        PlaySessionModel d = session(forca, day1, 20, PlaySessionStatus.FINISHED);
        when(sessions.findByUserIdAndPeriod(eq(userId), any(), any())).thenReturn(List.of(a, b, c, d));
        when(scores.findActiveByPlaySessionIds(anyCollection())).thenReturn(List.of(
            score(a, 100, "{\"errors\":0,\"correct\":1}"),
            score(b, 50, "{\"errors\":2}"),
            score(d, 80, "{\"errors\":2,\"correct\":5}")));

        ProgressResponse response = service.forCurrentUser(null, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 2));

        assertThat(response.series()).hasSize(3);
        ProgressPoint velhaDay1 = response.series().get(1);
        assertThat(velhaDay1.gameSlug()).isEqualTo("velha");
        assertThat(velhaDay1.date()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(velhaDay1.sessions()).isEqualTo(2);
        assertThat(velhaDay1.finished()).isEqualTo(2);
        assertThat(velhaDay1.averageDurationSeconds()).isEqualTo(40);
        assertThat(velhaDay1.averageScore()).isEqualTo(75.0);
        assertThat(velhaDay1.errors()).isEqualTo(2);
        assertThat(velhaDay1.correct()).isEqualTo(1);
        ProgressPoint velhaDay2 = response.series().get(2);
        assertThat(velhaDay2.abandoned()).isEqualTo(1);
        assertThat(velhaDay2.averageScore()).isNull();
        assertThat(response.series().get(0).gameSlug()).isEqualTo("forca");
    }

    @Test
    void filtroPorJogoUsaAConsultaEspecifica() {
        when(sessions.findByUserIdAndGameSlugAndPeriod(eq(userId), eq("forca"), any(), any())).thenReturn(List.of());

        ProgressResponse response = service.forCurrentUser(" Forca ", null, null);

        assertThat(response.game()).isEqualTo("forca");
        assertThat(response.series()).isEmpty();
        assertThat(response.to()).isEqualTo(response.from().plusDays(29));
    }

    @Test
    void periodoInvertidoOuLongoDemaisRetorna422() {
        assertThatThrownBy(() -> service.forCurrentUser(null, LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 1)))
            .isInstanceOf(ApiException.class)
            .extracting(e -> ((ApiException) e).getCode()).isEqualTo("PROGRESS_PERIOD_INVALID");
        assertThatThrownBy(() -> service.forCurrentUser(null, LocalDate.of(2024, 1, 1), LocalDate.of(2026, 1, 1)))
            .isInstanceOf(ApiException.class)
            .extracting(e -> ((ApiException) e).getCode()).isEqualTo("PROGRESS_PERIOD_TOO_LONG");
    }

    private PlaySessionModel session(GameModel game, Date startedAt, int seconds, PlaySessionStatus status) {
        PlaySessionModel session = new PlaySessionModel();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setGame(game);
        session.setStatus(status);
        session.setStartedAt(startedAt);
        session.setEndedAt(new Date(startedAt.getTime() + seconds * 1000L));
        return session;
    }

    private static ScoreModel score(PlaySessionModel session, int value, String breakdown) {
        ScoreModel score = new ScoreModel();
        score.setId(UUID.randomUUID());
        score.setPlaySession(session);
        score.setValue(value);
        score.setBreakdown(breakdown);
        return score;
    }
}
