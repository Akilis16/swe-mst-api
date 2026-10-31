package com.project.mst.module.tictactoe.dto;

import java.util.List;
import java.util.UUID;

import com.project.mst.module.scores.dto.ScoreResponse;
import com.project.mst.module.tictactoe.TicTacToeBoard;
import com.project.mst.module.tictactoe.TicTacToeMatchModel;
import com.project.mst.module.tictactoe.TicTacToeMode;
import com.project.mst.module.tictactoe.TicTacToeMoveModel;
import com.project.mst.module.tictactoe.TicTacToeResult;

public record TicTacToeMatchResponse(
    UUID matchId,
    UUID playSessionId,
    TicTacToeMode mode,
    List<List<String>> board,
    String nextMark,
    TicTacToeResult result,
    boolean finished,
    List<TicTacToeMoveResponse> moves,
    ScoreResponse score
) {

    public static TicTacToeMatchResponse from(TicTacToeMatchModel match, List<TicTacToeMoveModel> moves, ScoreResponse score) {
        TicTacToeBoard board = TicTacToeBoard.fromMoves(moves);
        TicTacToeResult result = match.getResult() != null ? match.getResult() : board.result();
        return new TicTacToeMatchResponse(
            match.getId(),
            match.getPlaySession().getId(),
            match.getMode(),
            board.rows(),
            result == null ? String.valueOf(board.nextMark()) : null,
            result,
            result != null,
            moves.stream().map(TicTacToeMoveResponse::from).toList(),
            score);
    }
}
