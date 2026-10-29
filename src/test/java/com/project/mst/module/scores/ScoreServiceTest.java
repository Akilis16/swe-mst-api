package com.project.mst.module.scores;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.project.mst.module.games.GameModel;
import com.project.mst.module.playsessions.PlaySessionModel;
import com.project.mst.module.playsessions.PlaySessionStatus;
import com.project.mst.module.scores.dto.ScoreResponse;
import com.project.mst.module.users.UserModel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ScoreServiceTest {

    @Mock ScoreRepository scores;

    @Test
    void semCalculadorDoJogoNaoPontua() {
        ScoreService service = new ScoreService(scores, List.of());
        PlaySessionModel session = finishedSession("velha");
        when(scores.findActiveByPlaySessionId(session.getId())).thenReturn(Optional.empty());

        assertThat(service.scoreFor(session)).isEmpty();
        verify(scores, never()).save(any());
    }

    @Test
    void pontuacaoLigaSessaoEGuardaComponentesDoCalculo() {
        ScoreService service = new ScoreService(scores, List.of(calculator("velha", 42, Map.of("moves", 5))));
        PlaySessionModel session = finishedSession("velha");
        when(scores.findActiveByPlaySessionId(session.getId())).thenReturn(Optional.empty());
        when(scores.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ScoreModel score = service.scoreFor(session).orElseThrow();

        assertThat(score.getValue()).isEqualTo(42);
        assertThat(score.getPlaySession()).isSameAs(session);
        assertThat(ScoreResponse.from(score).breakdown()).containsEntry("moves", 5);
    }

    @Test
    void sessaoJaPontuadaRetorna409() {
        ScoreService service = new ScoreService(scores, List.of(calculator("velha", 1, Map.of())));
        PlaySessionModel session = finishedSession("velha");
        when(scores.findActiveByPlaySessionId(session.getId())).thenReturn(Optional.of(new ScoreModel()));

        assertThatThrownBy(() -> service.scoreFor(session))
            .isInstanceOf(ResponseStatusException.class)
            .extracting(e -> ((ResponseStatusException) e).getStatusCode())
            .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void sessaoEmAndamentoNaoPontua() {
        ScoreService service = new ScoreService(scores, List.of(calculator("velha", 1, Map.of())));
        PlaySessionModel session = finishedSession("velha");
        session.setStatus(PlaySessionStatus.IN_PROGRESS);
        session.setEndedAt(null);

        assertThatThrownBy(() -> service.scoreFor(session)).isInstanceOf(IllegalStateException.class);
    }

    private static ScoreCalculator calculator(String slug, int value, Map<String, Object> breakdown) {
        return new ScoreCalculator() {
            @Override
            public String gameSlug() {
                return slug;
            }

            @Override
            public ScoreResult calculate(PlaySessionModel session) {
                return new ScoreResult(value, breakdown);
            }
        };
    }

    private static PlaySessionModel finishedSession(String slug) {
        UserModel user = new UserModel();
        user.setId(UUID.randomUUID());
        GameModel game = new GameModel();
        game.setId(UUID.randomUUID());
        game.setSlug(slug);

        PlaySessionModel session = new PlaySessionModel();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setGame(game);
        session.setStatus(PlaySessionStatus.FINISHED);
        session.setStartedAt(new Date(System.currentTimeMillis() - 1000));
        session.setEndedAt(new Date());
        return session;
    }
}
