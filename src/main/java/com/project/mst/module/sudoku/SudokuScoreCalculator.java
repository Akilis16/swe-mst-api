package com.project.mst.module.sudoku;

import java.util.LinkedHashMap;
import java.util.Map;

import com.project.mst.module.playsessions.Difficulty;
import com.project.mst.module.playsessions.PlaySessionModel;
import com.project.mst.module.playsessions.PlaySessionService;
import com.project.mst.module.scores.ScoreCalculator;
import com.project.mst.module.scores.ScoreResult;

import org.springframework.stereotype.Component;

@Component
public class SudokuScoreCalculator implements ScoreCalculator {

    static final int MAX = 100;
    static final int MISTAKE_PENALTY = 20;

    private final SudokuAttemptRepository attempts;

    public SudokuScoreCalculator(SudokuAttemptRepository attempts) {
        this.attempts = attempts;
    }

    @Override
    public String gameSlug() {
        return SudokuService.GAME_SLUG;
    }

    @Override
    public ScoreResult calculate(PlaySessionModel session) {
        SudokuAttemptModel attempt = attempts.findActiveByPlaySessionId(session.getId()).orElse(null);
        if (attempt == null) {
            return ScoreResult.of(0);
        }
        int mistakes = attempt.getMistakes() == null ? 0 : attempt.getMistakes();
        Difficulty difficulty = attempt.getPuzzle().getDifficulty();
        int value = attempt.isSolved() ? Math.max(0, MAX - mistakes * MISTAKE_PENALTY) * multiplier(difficulty) : 0;

        Map<String, Object> breakdown = new LinkedHashMap<>();
        breakdown.put("solved", attempt.isSolved());
        breakdown.put("difficulty", difficulty == null ? null : difficulty.name());
        breakdown.put("errors", mistakes);
        breakdown.put("timeLimitSeconds", attempt.getTimeLimitSeconds());
        breakdown.put("seconds", PlaySessionService.elapsedSeconds(session));
        return new ScoreResult(value, breakdown);
    }

    static int multiplier(Difficulty difficulty) {
        if (difficulty == null) {
            return 1;
        }
        return switch (difficulty) {
            case EASY -> 1;
            case MEDIUM -> 2;
            case HARD -> 3;
        };
    }
}
