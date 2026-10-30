package com.project.mst.module.playsessions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.project.mst.infra.security.Roles;
import com.project.mst.module.games.GameModel;
import com.project.mst.module.games.GameService;
import com.project.mst.module.playsessions.dto.PlaySessionResponse;
import com.project.mst.module.playsessions.dto.StartSessionRequest;
import com.project.mst.module.scores.ScoreModel;
import com.project.mst.module.scores.ScoreService;
import com.project.mst.module.scores.dto.ScoreResponse;
import com.project.mst.module.users.UserModel;
import com.project.mst.module.users.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PlaySessionServiceTest {

    @Mock PlaySessionRepository sessions;
    @Mock UserRepository users;
    @Mock GameService games;
    @Mock ScoreService scores;

    PlaySessionService service;

    UUID userId = UUID.randomUUID();
    UserModel user = new UserModel();
    GameModel game = new GameModel();

    @BeforeEach
    void setUp() {
        service = new PlaySessionService(sessions, users, games, scores);
        user.setId(userId);
        game.setId(UUID.randomUUID());
        game.setSlug("velha");
        authenticateAs(userId, Roles.PLAYER);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void iniciarUsaUsuarioDoTokenEJogoPeloSlug() {
        when(users.findActiveById(userId)).thenReturn(Optional.of(user));
        when(games.requireEnabledBySlug("velha")).thenReturn(game);
        when(sessions.save(any())).thenAnswer(saveWithId());

        PlaySessionResponse response = service.start(new StartSessionRequest("velha", Difficulty.EASY, false, "{\"device\":\"web\"}"));

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.gameSlug()).isEqualTo("velha");
        assertThat(response.status()).isEqualTo(PlaySessionStatus.IN_PROGRESS);
        assertThat(response.difficulty()).isEqualTo(Difficulty.EASY);
        assertThat(response.timed()).isFalse();
        assertThat(response.startedAt()).isNotNull();
        assertThat(response.endedAt()).isNull();
    }

    @Test
    void timedNuloUsaPreferenciaDoUsuario() {
        user.setPreferences("{\"timed\":false}");
        when(users.findActiveById(userId)).thenReturn(Optional.of(user));
        when(games.requireEnabledBySlug("velha")).thenReturn(game);
        when(sessions.save(any())).thenAnswer(saveWithId());

        assertThat(service.start(new StartSessionRequest("velha", null, null, null)).timed()).isFalse();
    }

    @Test
    void jogoInativoOuInexistenteRetorna404() {
        when(users.findActiveById(userId)).thenReturn(Optional.of(user));
        when(games.requireEnabledBySlug("2048")).thenThrow(new EntityNotFoundException("Jogo 2048 não encontrado"));

        assertThatThrownBy(() -> service.start(new StartSessionRequest("2048", null, null, null)))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void encerrarGravaFimNoServidorEPontuaPeloCalculador() {
        PlaySessionModel session = openSession();
        when(sessions.findActiveById(session.getId())).thenReturn(Optional.of(session));
        when(sessions.save(any())).thenAnswer(saveWithId());
        when(scores.scoreFor(any())).thenAnswer(invocation -> {
            ScoreModel score = new ScoreModel();
            score.setId(UUID.randomUUID());
            score.setPlaySession(invocation.getArgument(0));
            score.setValue(10);
            score.setBreakdown("{\"moves\":5}");
            return Optional.of(score);
        });

        PlaySessionResponse response = service.finish(session.getId());

        assertThat(response.status()).isEqualTo(PlaySessionStatus.FINISHED);
        assertThat(response.endedAt()).isAfterOrEqualTo(response.startedAt());
        assertThat(response.score().value()).isEqualTo(10);
        assertThat(response.score().breakdown()).containsEntry("moves", 5);
    }

    @Test
    void reenviarEncerramentoDevolveMesmoEstadoSemPontuarDeNovo() {
        PlaySessionModel session = openSession();
        session.setStatus(PlaySessionStatus.FINISHED);
        session.setEndedAt(new Date());
        ScoreModel existing = new ScoreModel();
        existing.setId(UUID.randomUUID());
        existing.setPlaySession(session);
        existing.setValue(7);
        when(sessions.findActiveById(session.getId())).thenReturn(Optional.of(session));
        when(scores.findByPlaySession(session.getId())).thenReturn(Optional.of(ScoreResponse.from(existing)));

        PlaySessionResponse response = service.finish(session.getId());

        assertThat(response.status()).isEqualTo(PlaySessionStatus.FINISHED);
        assertThat(response.score().value()).isEqualTo(7);
        verify(scores, never()).scoreFor(any());
        verify(sessions, never()).save(any());
    }

    @Test
    void sessaoAbandonadaNaoPodeSerEncerrada() {
        PlaySessionModel session = openSession();
        session.setStatus(PlaySessionStatus.ABANDONED);
        session.setEndedAt(new Date());
        when(sessions.findActiveById(session.getId())).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.finish(session.getId()))
            .isInstanceOf(ResponseStatusException.class)
            .extracting(e -> ((ResponseStatusException) e).getStatusCode())
            .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void abandonarSessaoEncerradaRetorna409() {
        PlaySessionModel session = openSession();
        session.setStatus(PlaySessionStatus.FINISHED);
        when(sessions.findActiveById(session.getId())).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.abandon(session.getId()))
            .isInstanceOf(ResponseStatusException.class)
            .extracting(e -> ((ResponseStatusException) e).getStatusCode())
            .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void sessaoDeOutroUsuarioRetorna403() {
        PlaySessionModel session = openSession();
        when(sessions.findActiveById(session.getId())).thenReturn(Optional.of(session));
        authenticateAs(UUID.randomUUID(), Roles.PLAYER);

        assertThatThrownBy(() -> service.finish(session.getId()))
            .isInstanceOf(ResponseStatusException.class)
            .extracting(e -> ((ResponseStatusException) e).getStatusCode())
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void adminPodeAbandonarSessaoDeOutroUsuario() {
        PlaySessionModel session = openSession();
        when(sessions.findActiveById(session.getId())).thenReturn(Optional.of(session));
        when(sessions.save(any())).thenAnswer(saveWithId());
        authenticateAs(UUID.randomUUID(), Roles.ADMIN);

        assertThat(service.abandon(session.getId()).status()).isEqualTo(PlaySessionStatus.ABANDONED);
    }

    @Test
    void clientMetaPrecisaSerJsonValido() {
        assertThatThrownBy(() -> service.start(new StartSessionRequest("velha", null, true, "{nao-e-json")))
            .isInstanceOf(ResponseStatusException.class)
            .extracting(e -> ((ResponseStatusException) e).getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void historicoDoJogadorFiltraPorJogoETrazPontuacao() {
        PlaySessionModel session = openSession();
        var pageable = PageRequest.of(0, 20);
        when(sessions.findPageByUserIdAndGameSlug(userId, "velha", pageable)).thenReturn(new PageImpl<>(List.of(session)));
        when(scores.findByPlaySession(session.getId())).thenReturn(Optional.empty());

        assertThat(service.pageForCurrentUser(" Velha ", pageable).getContent()).hasSize(1);
        verify(sessions, never()).findPageAll(any());
    }

    private PlaySessionModel openSession() {
        PlaySessionModel session = new PlaySessionModel();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setGame(game);
        session.setStatus(PlaySessionStatus.IN_PROGRESS);
        session.setTimed(true);
        session.setStartedAt(new Date(System.currentTimeMillis() - 1000));
        return session;
    }

    private static org.mockito.stubbing.Answer<Object> saveWithId() {
        return invocation -> {
            PlaySessionModel saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(UUID.randomUUID());
            }
            return saved;
        };
    }

    private static void authenticateAs(UUID id, String role) {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "HS256").subject(id.toString()).build();
        SecurityContextHolder.getContext().setAuthentication(
            new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }
}
