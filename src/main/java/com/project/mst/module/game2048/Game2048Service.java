package com.project.mst.module.game2048;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.project.mst.infra.controller.ApiException;
import com.project.mst.module.game2048.dto.Game2048MoveRequest;
import com.project.mst.module.game2048.dto.Game2048MoveResponse;
import com.project.mst.module.game2048.dto.Game2048RoundResponse;
import com.project.mst.module.game2048.dto.StartGame2048Request;
import com.project.mst.module.playsessions.PlaySessionModel;
import com.project.mst.module.playsessions.PlaySessionService;
import com.project.mst.module.scores.ScoreService;
import com.project.mst.module.scores.dto.ScoreResponse;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Service
public class Game2048Service {

    public static final String GAME_SLUG = "2048";

    private static final ObjectMapper JSON = JsonMapper.builder().build();
    private static final TypeReference<List<List<Integer>>> BOARD = new TypeReference<>() { };

    private final Game2048RoundRepository rounds;
    private final PlaySessionService sessions;
    private final ScoreService scores;
    private final Random random;

    @Autowired
    public Game2048Service(Game2048RoundRepository rounds, PlaySessionService sessions, ScoreService scores) {
        this(rounds, sessions, scores, new SecureRandom());
    }

    Game2048Service(Game2048RoundRepository rounds, PlaySessionService sessions, ScoreService scores, Random random) {
        this.rounds = rounds;
        this.sessions = sessions;
        this.scores = scores;
        this.random = random;
    }

    @Transactional
    public Game2048RoundResponse start(StartGame2048Request request) {
        PlaySessionModel session = sessions.open(GAME_SLUG, null, request == null ? null : request.timed());
        Game2048Board board = Game2048Board.start(random);

        Game2048RoundModel round = new Game2048RoundModel();
        round.setPlaySession(session);
        round.setBoard(JSON.writeValueAsString(board.rows()));
        round.setPoints(0);
        round.setMoves(0);
        round.setMaxTile(board.maxTile());
        round.setId_user_created(session.getUser().getId());
        round = rounds.save(round);

        return roundResponse(round, null);
    }

    @Transactional(readOnly = true)
    public Game2048RoundResponse find(UUID roundId) {
        Game2048RoundModel round = ownedRound(roundId);
        ScoreResponse score = round.getPlaySession().isOpen() ? null
            : scores.findByPlaySession(round.getPlaySession().getId()).orElse(null);
        return roundResponse(round, score);
    }

    @Transactional
    public Game2048MoveResponse move(UUID roundId, Game2048MoveRequest request) {
        Game2048RoundModel round = ownedRound(roundId);
        if (round.getResult() != null || !round.getPlaySession().isOpen()) {
            throw new ApiException(HttpStatus.CONFLICT, "GAME2048_ROUND_FINISHED", "Partida já encerrada");
        }

        Game2048Board board = board(round);
        Game2048Board.Slide slide = board.slide(request.direction());
        if (!slide.moved()) {
            return moveResponse(round, board, false, 0, null);
        }

        board.spawn(random);
        round.setBoard(JSON.writeValueAsString(board.rows()));
        round.setPoints(round.getPoints() + slide.gained());
        round.setMoves(round.getMoves() + 1);
        round.setMaxTile(board.maxTile());

        Game2048Result result = board.result();
        ScoreResponse score = null;
        if (result != null) {
            round.setResult(result);
        }
        round = rounds.save(round);
        if (result != null) {
            score = sessions.finish(round.getPlaySession().getId()).score();
        }
        return moveResponse(round, board, true, slide.gained(), score);
    }

    private static Game2048RoundResponse roundResponse(Game2048RoundModel round, ScoreResponse score) {
        boolean finished = round.getResult() != null || !round.getPlaySession().isOpen();
        return new Game2048RoundResponse(round.getId(), round.getPlaySession().getId(), board(round).rows(),
            round.getPoints(), round.getMoves(), round.getMaxTile(), finished, round.getResult(), score);
    }

    private static Game2048MoveResponse moveResponse(Game2048RoundModel round, Game2048Board board, boolean moved,
            int gained, ScoreResponse score) {
        boolean finished = round.getResult() != null;
        return new Game2048MoveResponse(moved, gained, board.rows(), round.getPoints(), round.getMoves(),
            round.getMaxTile(), finished, round.getResult(), score);
    }

    static Game2048Board board(Game2048RoundModel round) {
        return Game2048Board.of(JSON.readValue(round.getBoard(), BOARD));
    }

    private Game2048RoundModel ownedRound(UUID id) {
        Game2048RoundModel round = rounds.findActiveById(id)
            .orElseThrow(() -> new EntityNotFoundException("Partida " + id + " não encontrada"));
        sessions.requireOwner(round.getPlaySession());
        return round;
    }
}
