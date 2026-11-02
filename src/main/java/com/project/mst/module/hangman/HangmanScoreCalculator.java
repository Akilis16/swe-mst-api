package com.project.mst.module.hangman;

import java.util.LinkedHashMap;
import java.util.Map;

import com.project.mst.module.playsessions.PlaySessionModel;
import com.project.mst.module.playsessions.PlaySessionService;
import com.project.mst.module.scores.ScoreCalculator;
import com.project.mst.module.scores.ScoreResult;

import org.springframework.stereotype.Component;

@Component
public class HangmanScoreCalculator implements ScoreCalculator {

    static final int MAX = 100;
    static final int ERROR_PENALTY = 10;

    private final HangmanRoundRepository rounds;
    private final HangmanGuessRepository guesses;

    public HangmanScoreCalculator(HangmanRoundRepository rounds, HangmanGuessRepository guesses) {
        this.rounds = rounds;
        this.guesses = guesses;
    }

    @Override
    public String gameSlug() {
        return HangmanService.GAME_SLUG;
    }

    @Override
    public ScoreResult calculate(PlaySessionModel session) {
        HangmanRoundModel round = rounds.findActiveByPlaySessionId(session.getId()).orElse(null);
        if (round == null) {
            return ScoreResult.of(0);
        }
        long errors = guesses.findByRoundIdOrdered(round.getId()).stream().filter(g -> !g.isCorrect()).count();
        int value = round.getResult() == HangmanResult.WIN ? (int) Math.max(0, MAX - errors * ERROR_PENALTY) : 0;

        Map<String, Object> breakdown = new LinkedHashMap<>();
        breakdown.put("result", round.getResult() == null ? null : round.getResult().name());
        breakdown.put("errors", errors);
        breakdown.put("wordLength", round.getWord().getText().length());
        breakdown.put("category", round.getWord().getCategory());
        breakdown.put("seconds", PlaySessionService.elapsedSeconds(session));
        return new ScoreResult(value, breakdown);
    }
}
