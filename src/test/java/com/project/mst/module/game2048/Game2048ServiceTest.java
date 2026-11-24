package com.project.mst.module.game2048;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import com.project.mst.infra.controller.ApiException;
import com.project.mst.module.game2048.dto.Game2048MoveRequest;
import com.project.mst.module.game2048.dto.Game2048MoveResponse;
import com.project.mst.module.game2048.dto.Game2048RoundResponse;
import com.project.mst.module.game2048.dto.StartGame2048Request;
import com.project.mst.module.games.GameModel;
import com.project.mst.module.playsessions.PlaySessionModel;
import com.project.mst.module.playsessions.PlaySessionService;
import com.project.mst.module.playsessions.PlaySessionStatus;
import com.project.mst.module.playsessions.dto.PlaySessionResponse;
import com.project.mst.module.scores.ScoreService;
import com.project.mst.module.users.UserModel;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class Game2048ServiceTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    @Mock Game2048RoundRepository rounds;
    @Mock PlaySessionService sessions;
    @Mock ScoreService scores;

    Game2048Service service;
    Game2048RoundModel round = new Game2048RoundModel();
    PlaySessionModel session = new PlaySessionModel();

    @BeforeEach
    void setUp() {
        service = new Game2048Service(rounds, sessions, scores, new Random(3));
        UserModel user = new UserModel();
        user.setId(UUID.randomUUID());
        GameModel game = new GameModel();
        game.setSlug("2048");
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setGame(game);
        session.setStatus(PlaySessionStatus.IN_PROGRESS);
        session.setStartedAt(new Date());
        round.setId(UUID.randomUUID());
        round.setPlaySession(session);
        round.setPoints(0);
        round.setMoves(0);
    }

    @Test
    void iniciarAbreSessaoComDuasPecas() {
        when(sessions.open("2048", null, null)).thenReturn(session);
        when(rounds.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Game2048RoundResponse response = service.start(new StartGame2048Request(null));

        long twos = response.board().stream().flatMap(List::stream).filter(v -> v == 2).count();
        assertThat(twos).isEqualTo(2);
        assertThat(response.points()).isZero();
        assertThat(response.maxTile()).isEqualTo(2);
        assertThat(response.finished()).isFalse();
        assertThat(response.score()).isNull();
    }

    @Test
    void jogadaFundePecasSomaPontosESorteiaNovaPeca() {
        stubRound(List.of(
            List.of(2, 2, 0, 0),
            List.of(0, 0, 0, 0),
            List.of(0, 0, 0, 0),
            List.of(0, 0, 0, 0)));

        Game2048MoveResponse response = service.move(round.getId(), new Game2048MoveRequest(Game2048Direction.LEFT));

        assertThat(response.moved()).isTrue();
        assertThat(response.gained()).isEqualTo(4);
        assertThat(response.points()).isEqualTo(4);
        assertThat(response.moves()).isEqualTo(1);
        assertThat(response.board().get(0).get(0)).isEqualTo(4);
        long tiles = response.board().stream().flatMap(List::stream).filter(v -> v > 0).count();
        assertThat(tiles).isEqualTo(2);
        assertThat(response.finished()).isFalse();
        verify(sessions, never()).finish(any());
    }

    @Test
    void jogadaQueNaoMexeNaoSorteiaPecaNemContaMovimento() {
        stubRound(List.of(
            List.of(2, 4, 0, 0),
            List.of(0, 0, 0, 0),
            List.of(0, 0, 0, 0),
            List.of(0, 0, 0, 0)));

        Game2048MoveResponse response = service.move(round.getId(), new Game2048MoveRequest(Game2048Direction.LEFT));

        assertThat(response.moved()).isFalse();
        assertThat(response.moves()).isZero();
        long tiles = response.board().stream().flatMap(List::stream).filter(v -> v > 0).count();
        assertThat(tiles).isEqualTo(2);
        verify(rounds, never()).save(any());
    }

    @Test
    void chegarA2048EncerraComVitoriaEPontua() {
        stubRound(List.of(
            List.of(1024, 1024, 0, 0),
            List.of(0, 0, 0, 0),
            List.of(0, 0, 0, 0),
            List.of(0, 0, 0, 0)));
        when(rounds.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(sessions.finish(session.getId())).thenReturn(PlaySessionResponse.from(session, null));

        Game2048MoveResponse response = service.move(round.getId(), new Game2048MoveRequest(Game2048Direction.LEFT));

        assertThat(response.finished()).isTrue();
        assertThat(response.result()).isEqualTo(Game2048Result.WIN);
        assertThat(response.maxTile()).isEqualTo(2048);
        verify(sessions).finish(session.getId());

        assertThatThrownBy(() -> service.move(round.getId(), new Game2048MoveRequest(Game2048Direction.UP)))
            .isInstanceOf(ApiException.class)
            .extracting(e -> ((ApiException) e).getCode()).isEqualTo("GAME2048_ROUND_FINISHED");
    }

    @Test
    void partidaInexistenteRetorna404() {
        UUID id = UUID.randomUUID();
        when(rounds.findActiveById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.find(id)).isInstanceOf(EntityNotFoundException.class);
    }

    private void stubRound(List<List<Integer>> board) {
        round.setBoard(JSON.writeValueAsString(board));
        when(rounds.findActiveById(round.getId())).thenReturn(Optional.of(round));
        org.mockito.Mockito.lenient().when(rounds.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }
}
