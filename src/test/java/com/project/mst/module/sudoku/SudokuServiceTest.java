package com.project.mst.module.sudoku;

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
import com.project.mst.module.playsessions.Difficulty;
import com.project.mst.module.playsessions.PlaySessionModel;
import com.project.mst.module.playsessions.PlaySessionService;
import com.project.mst.module.playsessions.PlaySessionStatus;
import com.project.mst.module.playsessions.dto.PlaySessionResponse;
import com.project.mst.module.scores.ScoreService;
import com.project.mst.module.sudoku.dto.EntryRequest;
import com.project.mst.module.sudoku.dto.EntryResponse;
import com.project.mst.module.sudoku.dto.StartSudokuRequest;
import com.project.mst.module.sudoku.dto.SudokuAttemptResponse;
import com.project.mst.module.users.UserModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SudokuServiceTest {

    static final String SOLUTION = "534678912672195348198342567859761423426853791713924856961537284287419635345286179";

    @Mock SudokuPuzzleRepository puzzles;
    @Mock SudokuAttemptRepository attempts;
    @Mock SudokuEntryRepository entries;
    @Mock PlaySessionService sessions;
    @Mock ScoreService scores;

    SudokuService service;
    SudokuPuzzleModel puzzle = new SudokuPuzzleModel();
    SudokuAttemptModel attempt = new SudokuAttemptModel();
    PlaySessionModel session = new PlaySessionModel();
    List<SudokuEntryModel> stored = new ArrayList<>();

    @BeforeEach
    void setUp() {
        service = new SudokuService(puzzles, attempts, entries, sessions, scores, new Random(3));
        puzzle.setId(UUID.randomUUID());
        puzzle.setDifficulty(Difficulty.EASY);
        puzzle.setSolution(SOLUTION);
        puzzle.setGivens("0" + SOLUTION.substring(1, 80) + "0");
        UserModel user = new UserModel();
        user.setId(UUID.randomUUID());
        GameModel game = new GameModel();
        game.setSlug("sudoku");
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setGame(game);
        session.setStatus(PlaySessionStatus.IN_PROGRESS);
        session.setStartedAt(new Date());
        attempt.setId(UUID.randomUUID());
        attempt.setPuzzle(puzzle);
        attempt.setPlaySession(session);
        attempt.setMistakes(0);
    }

    @Test
    void iniciarSorteiaPuzzleDoNivelESoDevolveAsCasasDadas() {
        when(puzzles.findEnabledByDifficulty(Difficulty.EASY)).thenReturn(List.of(puzzle));
        when(sessions.open("sudoku", Difficulty.EASY, false)).thenReturn(session);
        when(attempts.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SudokuAttemptResponse response = service.start(new StartSudokuRequest(Difficulty.EASY, null));

        assertThat(response.givens()).isEqualTo(puzzle.getGivens());
        assertThat(response.remainingCells()).isEqualTo(2);
        assertThat(response.mistakesAllowed()).isEqualTo(3);
        assertThat(response.timeLimitSeconds()).isNull();
    }

    @Test
    void limiteDeTempoForaDaListaRetorna422() {
        assertThatThrownBy(() -> service.start(new StartSudokuRequest(Difficulty.HARD, 120)))
            .isInstanceOf(ApiException.class)
            .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SUDOKU_TIME_LIMIT_INVALID");
    }

    @Test
    void valorErradoContaErroEEApagadoDaGrade() {
        stubAttempt();

        EntryResponse wrong = service.entry(attempt.getId(), new EntryRequest(0, 1));

        assertThat(wrong.correct()).isFalse();
        assertThat(wrong.mistakes()).isEqualTo(1);
        assertThat(wrong.remainingCells()).isEqualTo(2);
        assertThat(service.find(attempt.getId()).grid()).startsWith("0");
    }

    @Test
    void casaDadaNaoPodeSerAlterada() {
        stubAttempt();

        assertThatThrownBy(() -> service.entry(attempt.getId(), new EntryRequest(1, 3)))
            .isInstanceOf(ApiException.class)
            .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SUDOKU_CELL_IS_GIVEN");
    }

    @Test
    void tresErrosEncerramComDerrota() {
        stubAttempt();
        when(sessions.finish(session.getId())).thenReturn(PlaySessionResponse.from(session, null));

        service.entry(attempt.getId(), new EntryRequest(0, 1));
        service.entry(attempt.getId(), new EntryRequest(0, 2));
        EntryResponse third = service.entry(attempt.getId(), new EntryRequest(0, 4));

        assertThat(third.finished()).isTrue();
        assertThat(third.result()).isEqualTo(SudokuResult.LOSS);
        assertThat(attempt.isSolved()).isFalse();
        verify(sessions).finish(session.getId());
    }

    @Test
    void ultimaCasaCorretaEncerraComVitoria() {
        stubAttempt();
        when(sessions.finish(session.getId())).thenReturn(PlaySessionResponse.from(session, null));

        service.entry(attempt.getId(), new EntryRequest(0, 5));
        EntryResponse last = service.entry(attempt.getId(), new EntryRequest(80, 9));

        assertThat(last.correct()).isTrue();
        assertThat(last.remainingCells()).isZero();
        assertThat(last.result()).isEqualTo(SudokuResult.WIN);
        assertThat(attempt.isSolved()).isTrue();
    }

    @Test
    void tempoEsgotadoAbandonaSemAceitarAJogada() {
        attempt.setTimeLimitSeconds(180);
        session.setStartedAt(new Date(System.currentTimeMillis() - 200_000));
        stubAttempt();
        when(sessions.abandon(session.getId())).thenReturn(PlaySessionResponse.from(session, null));

        EntryResponse response = service.entry(attempt.getId(), new EntryRequest(0, 5));

        assertThat(response.finished()).isTrue();
        assertThat(response.result()).isEqualTo(SudokuResult.TIMEOUT);
        verify(entries, never()).save(any());
    }

    @Test
    void revelarDevolveASolucaoEAbandonaASessao() {
        when(attempts.findActiveById(attempt.getId())).thenReturn(Optional.of(attempt));
        when(sessions.abandon(session.getId())).thenReturn(PlaySessionResponse.from(session, null));

        assertThat(service.reveal(attempt.getId()).solution()).isEqualTo(SOLUTION);
        verify(sessions).abandon(session.getId());
        verify(sessions, never()).finish(any());
    }

    private void stubAttempt() {
        when(attempts.findActiveById(attempt.getId())).thenReturn(Optional.of(attempt));
        when(entries.findByAttemptIdOrdered(attempt.getId())).thenAnswer(inv -> new ArrayList<>(stored));
        lenient().when(entries.save(any())).thenAnswer(inv -> {
            stored.add(inv.getArgument(0));
            return inv.getArgument(0);
        });
        lenient().when(attempts.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }
}
