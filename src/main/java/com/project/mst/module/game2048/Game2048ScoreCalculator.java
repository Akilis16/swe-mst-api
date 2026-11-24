package com.project.mst.module.game2048;

import java.util.LinkedHashMap;
import java.util.Map;

import com.project.mst.module.playsessions.PlaySessionModel;
import com.project.mst.module.playsessions.PlaySessionService;
import com.project.mst.module.scores.ScoreCalculator;
import com.project.mst.module.scores.ScoreResult;

import org.springframework.stereotype.Component;

@Component
public class Game2048ScoreCalculator implements ScoreCalculator {

    private final Game2048RoundRepository rounds;

    public Game2048ScoreCalculator(Game2048RoundRepository rounds) {
        this.rounds = rounds;
    }

    @Override
    public String gameSlug() {
        return Game2048Service.GAME_SLUG;
    }

    @Override
    public ScoreResult calculate(PlaySessionModel session) {
        Game2048RoundModel round = rounds.findActiveByPlaySessionId(session.getId()).orElse(null);
        if (round == null) {
            return ScoreResult.of(0);
        }
        int points = round.getPoints() == null ? 0 : round.getPoints();

        Map<String, Object> breakdown = new LinkedHashMap<>();
        breakdown.put("result", round.getResult() == null ? null : round.getResult().name());
        breakdown.put("points", points);
        breakdown.put("maxTile", round.getMaxTile());
        breakdown.put("moves", round.getMoves());
        breakdown.put("seconds", PlaySessionService.elapsedSeconds(session));
        return new ScoreResult(points, breakdown);
    }
}
