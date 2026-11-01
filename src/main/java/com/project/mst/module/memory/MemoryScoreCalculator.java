package com.project.mst.module.memory;

import java.util.LinkedHashMap;
import java.util.Map;

import com.project.mst.module.playsessions.PlaySessionModel;
import com.project.mst.module.playsessions.PlaySessionService;
import com.project.mst.module.scores.ScoreCalculator;
import com.project.mst.module.scores.ScoreResult;

import org.springframework.stereotype.Component;

@Component
public class MemoryScoreCalculator implements ScoreCalculator {

    static final int MAX = 100;
    static final int ERROR_PENALTY = 5;

    private final MemoryRoundRepository rounds;

    public MemoryScoreCalculator(MemoryRoundRepository rounds) {
        this.rounds = rounds;
    }

    @Override
    public String gameSlug() {
        return MemoryService.GAME_SLUG;
    }

    @Override
    public ScoreResult calculate(PlaySessionModel session) {
        MemoryRoundModel round = rounds.findActiveByPlaySessionId(session.getId()).orElse(null);
        if (round == null) {
            return ScoreResult.of(0);
        }
        int errors = round.getErrors() == null ? 0 : round.getErrors();
        int value = Math.max(0, MAX - errors * ERROR_PENALTY);

        Map<String, Object> breakdown = new LinkedHashMap<>();
        breakdown.put("pairs", round.getPairs());
        breakdown.put("errors", errors);
        breakdown.put("seconds", PlaySessionService.elapsedSeconds(session));
        return new ScoreResult(value, breakdown);
    }
}
