package com.project.mst.module.tictactoe;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.project.mst.infra.controller.ApiException;
import com.project.mst.module.playsessions.PlaySessionModel;
import com.project.mst.module.playsessions.PlaySessionService;
import com.project.mst.module.scores.ScoreService;
import com.project.mst.module.scores.dto.ScoreResponse;
import com.project.mst.module.tictactoe.dto.MoveRequest;
import com.project.mst.module.tictactoe.dto.StartMatchRequest;
import com.project.mst.module.tictactoe.dto.TicTacToeMatchResponse;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicTacToeService {

    public static final String GAME_SLUG = "velha";

    private final TicTacToeMatchRepository matches;
    private final TicTacToeMoveRepository moves;
    private final PlaySessionService sessions;
    private final ScoreService scores;
    private final Random random;

    @Autowired
    public TicTacToeService(TicTacToeMatchRepository matches, TicTacToeMoveRepository moves,
            PlaySessionService sessions, ScoreService scores) {
        this(matches, moves, sessions, scores, new SecureRandom());
    }

    TicTacToeService(TicTacToeMatchRepository matches, TicTacToeMoveRepository moves,
            PlaySessionService sessions, ScoreService scores, Random random) {
        this.matches = matches;
        this.moves = moves;
        this.sessions = sessions;
        this.scores = scores;
        this.random = random;
    }

    @Transactional
    public TicTacToeMatchResponse start(StartMatchRequest request) {
        PlaySessionModel session = sessions.open(GAME_SLUG, null, request.timed());

        TicTacToeMatchModel match = new TicTacToeMatchModel();
        match.setPlaySession(session);
        match.setMode(request.mode());
        match.setId_user_created(session.getUser().getId());
        match = matches.save(match);

        return TicTacToeMatchResponse.from(match, List.of(), null);
    }

    @Transactional(readOnly = true)
    public TicTacToeMatchResponse find(UUID matchId) {
        TicTacToeMatchModel match = ownedMatch(matchId);
        return response(match, moves.findByMatchIdOrdered(match.getId()));
    }

    @Transactional
    public TicTacToeMatchResponse move(UUID matchId, MoveRequest request) {
        TicTacToeMatchModel match = ownedMatch(matchId);
        if (match.getResult() != null || !match.getPlaySession().isOpen()) {
            throw new ApiException(HttpStatus.CONFLICT, "TICTACTOE_MATCH_FINISHED", "Partida já encerrada");
        }

        List<TicTacToeMoveModel> played = moves.findByMatchIdOrdered(match.getId());
        TicTacToeBoard board = TicTacToeBoard.fromMoves(played);
        char mark = board.nextMark();
        if (match.getMode() == TicTacToeMode.VS_COMPUTER && mark != TicTacToeBoard.X) {
            throw new ApiException(HttpStatus.CONFLICT, "TICTACTOE_NOT_YOUR_TURN", "Vez do computador");
        }
        if (!board.isEmpty(request.row(), request.col())) {
            throw new ApiException(HttpStatus.CONFLICT, "TICTACTOE_CELL_OCCUPIED", "Casa já ocupada");
        }

        played.add(record(match, played.size() + 1, request.row(), request.col(), mark, board));

        if (!board.isFinished() && match.getMode() == TicTacToeMode.VS_COMPUTER) {
            List<int[]> empty = board.emptyCells();
            int[] cell = empty.get(random.nextInt(empty.size()));
            played.add(record(match, played.size() + 1, cell[0], cell[1], TicTacToeBoard.O, board));
        }

        TicTacToeResult result = board.result();
        if (result != null) {
            match.setResult(result);
            match = matches.save(match);
            ScoreResponse score = sessions.finish(match.getPlaySession().getId()).score();
            return TicTacToeMatchResponse.from(match, played, score);
        }
        return TicTacToeMatchResponse.from(match, played, null);
    }

    private TicTacToeMoveModel record(TicTacToeMatchModel match, int number, int row, int col, char mark, TicTacToeBoard board) {
        board.place(row, col, mark);
        TicTacToeMoveModel move = new TicTacToeMoveModel();
        move.setMatch(match);
        move.setMoveNumber(number);
        move.setRowIdx(row);
        move.setColIdx(col);
        move.setMark(String.valueOf(mark));
        move.setId_user_created(match.getPlaySession().getUser().getId());
        return moves.save(move);
    }

    private TicTacToeMatchResponse response(TicTacToeMatchModel match, List<TicTacToeMoveModel> played) {
        ScoreResponse score = match.getResult() == null ? null
            : scores.findByPlaySession(match.getPlaySession().getId()).orElse(null);
        return TicTacToeMatchResponse.from(match, played, score);
    }

    private TicTacToeMatchModel ownedMatch(UUID id) {
        TicTacToeMatchModel match = matches.findActiveById(id)
            .orElseThrow(() -> new EntityNotFoundException("Partida " + id + " não encontrada"));
        sessions.requireOwner(match.getPlaySession());
        return match;
    }
}
