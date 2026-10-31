package com.project.mst.module.tictactoe;

import java.util.LinkedHashMap;
import java.util.Map;

import com.project.mst.module.playsessions.PlaySessionModel;
import com.project.mst.module.playsessions.PlaySessionService;
import com.project.mst.module.scores.ScoreCalculator;
import com.project.mst.module.scores.ScoreResult;

import org.springframework.stereotype.Component;

@Component
public class TicTacToeScoreCalculator implements ScoreCalculator {

    static final int WIN = 100;
    static final int DRAW = 50;
    static final int LOSS = 10;
    static final int LOCAL_MATCH = 50;

    private final TicTacToeMatchRepository matches;
    private final TicTacToeMoveRepository moves;

    public TicTacToeScoreCalculator(TicTacToeMatchRepository matches, TicTacToeMoveRepository moves) {
        this.matches = matches;
        this.moves = moves;
    }

    @Override
    public String gameSlug() {
        return TicTacToeService.GAME_SLUG;
    }

    @Override
    public ScoreResult calculate(PlaySessionModel session) {
        TicTacToeMatchModel match = matches.findActiveByPlaySessionId(session.getId()).orElse(null);
        if (match == null) {
            return ScoreResult.of(0);
        }
        TicTacToeResult result = match.getResult();
        int value = switch (match.getMode()) {
            case LOCAL_2P -> result == null ? 0 : LOCAL_MATCH;
            case VS_COMPUTER -> result == null ? 0 : switch (result) {
                case X_WINS -> WIN;
                case DRAW -> DRAW;
                case O_WINS -> LOSS;
            };
        };

        Map<String, Object> breakdown = new LinkedHashMap<>();
        breakdown.put("mode", match.getMode().name());
        breakdown.put("result", result == null ? null : result.name());
        breakdown.put("moves", moves.findByMatchIdOrdered(match.getId()).size());
        breakdown.put("seconds", PlaySessionService.elapsedSeconds(session));
        return new ScoreResult(value, breakdown);
    }
}
