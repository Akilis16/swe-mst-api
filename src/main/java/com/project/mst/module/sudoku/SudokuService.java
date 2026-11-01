package com.project.mst.module.sudoku;

import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import com.project.mst.infra.controller.ApiException;
import com.project.mst.module.playsessions.PlaySessionModel;
import com.project.mst.module.playsessions.PlaySessionService;
import com.project.mst.module.playsessions.PlaySessionStatus;
import com.project.mst.module.scores.ScoreService;
import com.project.mst.module.scores.dto.ScoreResponse;
import com.project.mst.module.sudoku.dto.EntryRequest;
import com.project.mst.module.sudoku.dto.EntryResponse;
import com.project.mst.module.sudoku.dto.RevealResponse;
import com.project.mst.module.sudoku.dto.StartSudokuRequest;
import com.project.mst.module.sudoku.dto.SudokuAttemptResponse;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SudokuService {

    public static final String GAME_SLUG = "sudoku";
    public static final int MISTAKES_ALLOWED = 3;
    public static final Set<Integer> TIME_LIMITS = Set.of(180, 300, 600);

    private final SudokuPuzzleRepository puzzles;
    private final SudokuAttemptRepository attempts;
    private final SudokuEntryRepository entries;
    private final PlaySessionService sessions;
    private final ScoreService scores;
    private final Random random;

    @Autowired
    public SudokuService(SudokuPuzzleRepository puzzles, SudokuAttemptRepository attempts, SudokuEntryRepository entries,
            PlaySessionService sessions, ScoreService scores) {
        this(puzzles, attempts, entries, sessions, scores, new SecureRandom());
    }

    SudokuService(SudokuPuzzleRepository puzzles, SudokuAttemptRepository attempts, SudokuEntryRepository entries,
            PlaySessionService sessions, ScoreService scores, Random random) {
        this.puzzles = puzzles;
        this.attempts = attempts;
        this.entries = entries;
        this.sessions = sessions;
        this.scores = scores;
        this.random = random;
    }

    @Transactional
    public SudokuAttemptResponse start(StartSudokuRequest request) {
        if (request.timeLimitSeconds() != null && !TIME_LIMITS.contains(request.timeLimitSeconds())) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "SUDOKU_TIME_LIMIT_INVALID", "timeLimitSeconds precisa ser 180, 300, 600 ou nulo");
        }
        List<SudokuPuzzleModel> candidates = puzzles.findEnabledByDifficulty(request.difficulty());
        if (candidates.isEmpty()) {
            throw new EntityNotFoundException("Nenhum puzzle ativo no nível " + request.difficulty());
        }
        SudokuPuzzleModel puzzle = candidates.get(random.nextInt(candidates.size()));

        PlaySessionModel session = sessions.open(GAME_SLUG, request.difficulty(), request.timeLimitSeconds() != null);
        SudokuAttemptModel attempt = new SudokuAttemptModel();
        attempt.setPlaySession(session);
        attempt.setPuzzle(puzzle);
        attempt.setMistakes(0);
        attempt.setTimeLimitSeconds(request.timeLimitSeconds());
        attempt.setSolved(false);
        attempt.setId_user_created(session.getUser().getId());
        attempt = attempts.save(attempt);

        return response(attempt, List.of(), null, null);
    }

    @Transactional(readOnly = true)
    public SudokuAttemptResponse find(UUID attemptId) {
        SudokuAttemptModel attempt = ownedAttempt(attemptId);
        List<SudokuEntryModel> list = entries.findByAttemptIdOrdered(attempt.getId());
        ScoreResponse score = attempt.getPlaySession().isOpen() ? null
            : scores.findByPlaySession(attempt.getPlaySession().getId()).orElse(null);
        return response(attempt, list, resultOf(attempt, list), score);
    }

    @Transactional
    public EntryResponse entry(UUID attemptId, EntryRequest request) {
        SudokuAttemptModel attempt = ownedAttempt(attemptId);
        if (!attempt.getPlaySession().isOpen()) {
            throw new ApiException(HttpStatus.CONFLICT, "SUDOKU_ATTEMPT_FINISHED", "Tentativa já encerrada");
        }
        List<SudokuEntryModel> list = entries.findByAttemptIdOrdered(attempt.getId());
        SudokuGrid grid = SudokuGrid.of(attempt.getPuzzle().getGivens(), list);

        if (timedOut(attempt)) {
            sessions.abandon(attempt.getPlaySession().getId());
            return new EntryResponse(false, attempt.getMistakes(), MISTAKES_ALLOWED, grid.remainingCells(), true, SudokuResult.TIMEOUT, null);
        }
        if (grid.isGiven(request.cellIdx())) {
            throw new ApiException(HttpStatus.CONFLICT, "SUDOKU_CELL_IS_GIVEN", "Casa dada não pode ser alterada");
        }

        int expected = attempt.getPuzzle().getSolution().charAt(request.cellIdx()) - '0';
        boolean correct = request.value() == null || request.value() == expected;

        SudokuEntryModel entry = new SudokuEntryModel();
        entry.setAttempt(attempt);
        entry.setCellIdx(request.cellIdx());
        entry.setValue(request.value());
        entry.setCorrect(correct);
        entry.setEnteredAt(new Date());
        entry.setId_user_created(attempt.getPlaySession().getUser().getId());
        entries.save(entry);

        if (correct) {
            grid.set(request.cellIdx(), request.value() == null ? 0 : request.value());
        } else {
            attempt.setMistakes(attempt.getMistakes() + 1);
        }

        SudokuResult result = null;
        ScoreResponse score = null;
        if (grid.remainingCells() == 0) {
            attempt.setSolved(true);
            result = SudokuResult.WIN;
        } else if (attempt.getMistakes() >= MISTAKES_ALLOWED) {
            result = SudokuResult.LOSS;
        }
        attempts.save(attempt);
        if (result != null) {
            score = sessions.finish(attempt.getPlaySession().getId()).score();
        }

        return new EntryResponse(correct, attempt.getMistakes(), MISTAKES_ALLOWED, grid.remainingCells(), result != null, result, score);
    }

    @Transactional
    public RevealResponse reveal(UUID attemptId) {
        SudokuAttemptModel attempt = ownedAttempt(attemptId);
        if (!attempt.getPlaySession().isOpen()) {
            throw new ApiException(HttpStatus.CONFLICT, "SUDOKU_ATTEMPT_FINISHED", "Tentativa já encerrada");
        }
        sessions.abandon(attempt.getPlaySession().getId());
        return new RevealResponse(attempt.getPuzzle().getSolution());
    }

    static boolean timedOut(SudokuAttemptModel attempt) {
        Integer limit = attempt.getTimeLimitSeconds();
        return limit != null && PlaySessionService.elapsedSeconds(attempt.getPlaySession()) >= limit;
    }

    private static SudokuResult resultOf(SudokuAttemptModel attempt, List<SudokuEntryModel> list) {
        PlaySessionModel session = attempt.getPlaySession();
        if (session.isOpen()) {
            return null;
        }
        if (attempt.isSolved()) {
            return SudokuResult.WIN;
        }
        if (session.getStatus() == PlaySessionStatus.FINISHED) {
            return SudokuResult.LOSS;
        }
        return timedOut(attempt) && attempt.getMistakes() < MISTAKES_ALLOWED ? SudokuResult.TIMEOUT : SudokuResult.REVEALED;
    }

    private static SudokuAttemptResponse response(SudokuAttemptModel attempt, List<SudokuEntryModel> list,
            SudokuResult result, ScoreResponse score) {
        SudokuGrid grid = SudokuGrid.of(attempt.getPuzzle().getGivens(), list);
        return new SudokuAttemptResponse(attempt.getId(), attempt.getPlaySession().getId(),
            attempt.getPuzzle().getDifficulty(), attempt.getPuzzle().getGivens(), grid.toRowMajor(),
            attempt.getMistakes(), MISTAKES_ALLOWED, attempt.getTimeLimitSeconds(),
            PlaySessionService.elapsedSeconds(attempt.getPlaySession()), grid.remainingCells(),
            !attempt.getPlaySession().isOpen(), result, score);
    }

    private SudokuAttemptModel ownedAttempt(UUID id) {
        SudokuAttemptModel attempt = attempts.findActiveById(id)
            .orElseThrow(() -> new EntityNotFoundException("Tentativa " + id + " não encontrada"));
        sessions.requireOwner(attempt.getPlaySession());
        return attempt;
    }
}
