package com.project.mst.module.tictactoe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import com.project.mst.infra.controller.ApiException;
import com.project.mst.module.games.GameModel;
import com.project.mst.module.playsessions.PlaySessionModel;
import com.project.mst.module.playsessions.PlaySessionService;
import com.project.mst.module.playsessions.PlaySessionStatus;
import com.project.mst.module.playsessions.dto.PlaySessionResponse;
import com.project.mst.module.scores.ScoreService;
import com.project.mst.module.tictactoe.dto.MoveRequest;
import com.project.mst.module.tictactoe.dto.StartMatchRequest;
import com.project.mst.module.tictactoe.dto.TicTacToeMatchResponse;
import com.project.mst.module.users.UserModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TicTacToeServiceTest {

    @Mock TicTacToeMatchRepository matches;
    @Mock TicTacToeMoveRepository moves;
    @Mock PlaySessionService sessions;
    @Mock ScoreService scores;

    TicTacToeService service;
    PlaySessionModel session;
    TicTacToeMatchModel match;
    List<TicTacToeMoveModel> stored = new ArrayList<>();

    @BeforeEach
    void setUp() {
        service = new TicTacToeService(matches, moves, sessions, scores, new Random(7));
        UserModel user = new UserModel();
        user.setId(UUID.randomUUID());
        GameModel game = new GameModel();
        game.setSlug("velha");
        session = new PlaySessionModel();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setGame(game);
        session.setStatus(PlaySessionStatus.IN_PROGRESS);
        session.setStartedAt(new Date());
        match = new TicTacToeMatchModel();
        match.setId(UUID.randomUUID());
        match.setPlaySession(session);
        match.setMode(TicTacToeMode.VS_COMPUTER);
    }

    @Test
    void iniciarAbreSessaoNoJogoVelha() {
        when(sessions.open("velha", null, true)).thenReturn(session);
        when(matches.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TicTacToeMatchResponse response = service.start(new StartMatchRequest(TicTacToeMode.LOCAL_2P, true));

        assertThat(response.playSessionId()).isEqualTo(session.getId());
        assertThat(response.nextMark()).isEqualTo("X");
        assertThat(response.result()).isNull();
        assertThat(response.board()).hasSize(3);
    }

    @Test
    void contraOComputadorOServidorRespondeEmCasaVazia() {
        stubMatch();

        TicTacToeMatchResponse response = service.move(match.getId(), new MoveRequest(1, 1));

        assertThat(response.moves()).hasSize(2);
        assertThat(response.moves().get(0).mark()).isEqualTo("X");
        assertThat(response.moves().get(1).mark()).isEqualTo("O");
        assertThat(response.moves().get(1).row() * 3 + response.moves().get(1).col()).isNotEqualTo(4);
        assertThat(response.nextMark()).isEqualTo("X");
        verify(sessions, never()).finish(any());
    }

    @Test
    void casaOcupadaRetorna409() {
        stubMatch();
        stored.add(move(1, 0, 0, "X"));
        stored.add(move(2, 1, 1, "O"));

        assertThatThrownBy(() -> service.move(match.getId(), new MoveRequest(1, 1)))
            .isInstanceOf(ApiException.class)
            .extracting(e -> ((ApiException) e).getCode())
            .isEqualTo("TICTACTOE_CELL_OCCUPIED");
    }

    @Test
    void partidaEncerradaNaoAceitaJogada() {
        match.setResult(TicTacToeResult.DRAW);
        when(matches.findActiveById(match.getId())).thenReturn(Optional.of(match));

        assertThatThrownBy(() -> service.move(match.getId(), new MoveRequest(0, 0)))
            .isInstanceOf(ApiException.class)
            .extracting(e -> ((ApiException) e).getCode())
            .isEqualTo("TICTACTOE_MATCH_FINISHED");
    }

    @Test
    void vitoriaDoJogadorEncerraASessaoEPontua() {
        stubMatch();
        stored.add(move(1, 0, 0, "X"));
        stored.add(move(2, 1, 0, "O"));
        stored.add(move(3, 0, 1, "X"));
        stored.add(move(4, 1, 1, "O"));
        when(sessions.finish(session.getId())).thenReturn(PlaySessionResponse.from(session, null));

        TicTacToeMatchResponse response = service.move(match.getId(), new MoveRequest(0, 2));

        assertThat(response.result()).isEqualTo(TicTacToeResult.X_WINS);
        assertThat(response.finished()).isTrue();
        assertThat(response.nextMark()).isNull();
        assertThat(match.getResult()).isEqualTo(TicTacToeResult.X_WINS);
        verify(sessions).finish(session.getId());
    }

    @Test
    void modoLocalAlternaAsMarcasSemJogadaDoServidor() {
        match.setMode(TicTacToeMode.LOCAL_2P);
        stubMatch();
        stored.add(move(1, 0, 0, "X"));

        TicTacToeMatchResponse response = service.move(match.getId(), new MoveRequest(2, 2));

        assertThat(response.moves()).hasSize(2);
        assertThat(response.moves().get(1).mark()).isEqualTo("O");
        assertThat(response.nextMark()).isEqualTo("X");
    }

    private void stubMatch() {
        when(matches.findActiveById(match.getId())).thenReturn(Optional.of(match));
        when(moves.findByMatchIdOrdered(match.getId())).thenAnswer(inv -> new ArrayList<>(stored));
        lenient().when(moves.save(any())).thenAnswer(inv -> {
            TicTacToeMoveModel saved = inv.getArgument(0);
            stored.add(saved);
            return saved;
        });
        lenient().when(matches.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private TicTacToeMoveModel move(int number, int row, int col, String mark) {
        TicTacToeMoveModel move = new TicTacToeMoveModel();
        move.setMatch(match);
        move.setMoveNumber(number);
        move.setRowIdx(row);
        move.setColIdx(col);
        move.setMark(mark);
        return move;
    }
}
