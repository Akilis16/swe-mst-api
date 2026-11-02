package com.project.mst.module.hangman;

import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import com.project.mst.infra.controller.ApiException;
import com.project.mst.module.hangman.dto.CategoryResponse;
import com.project.mst.module.hangman.dto.GuessRequest;
import com.project.mst.module.hangman.dto.GuessResponse;
import com.project.mst.module.hangman.dto.HangmanRoundResponse;
import com.project.mst.module.hangman.dto.StartHangmanRequest;
import com.project.mst.module.playsessions.PlaySessionModel;
import com.project.mst.module.playsessions.PlaySessionService;
import com.project.mst.module.scores.ScoreService;
import com.project.mst.module.scores.dto.ScoreResponse;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HangmanService {

    public static final String GAME_SLUG = "forca";
    public static final int MAX_ERRORS = 6;

    private final WordRepository words;
    private final HangmanRoundRepository rounds;
    private final HangmanGuessRepository guesses;
    private final PlaySessionService sessions;
    private final ScoreService scores;
    private final Random random;

    @Autowired
    public HangmanService(WordRepository words, HangmanRoundRepository rounds, HangmanGuessRepository guesses,
            PlaySessionService sessions, ScoreService scores) {
        this(words, rounds, guesses, sessions, scores, new SecureRandom());
    }

    HangmanService(WordRepository words, HangmanRoundRepository rounds, HangmanGuessRepository guesses,
            PlaySessionService sessions, ScoreService scores, Random random) {
        this.words = words;
        this.rounds = rounds;
        this.guesses = guesses;
        this.sessions = sessions;
        this.scores = scores;
        this.random = random;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> categories() {
        return words.countEnabledByCategory().stream()
            .map(row -> new CategoryResponse((String) row[0], HangmanWord.displayName((String) row[0]), ((Number) row[1]).longValue()))
            .toList();
    }

    @Transactional
    public HangmanRoundResponse start(StartHangmanRequest request) {
        String category = request.category().trim().toUpperCase(Locale.ROOT);
        List<WordModel> candidates = words.findEnabledByCategory(category);
        if (candidates.isEmpty()) {
            throw new EntityNotFoundException("Categoria " + category + " não encontrada");
        }
        WordModel word = candidates.get(random.nextInt(candidates.size()));

        PlaySessionModel session = sessions.open(GAME_SLUG, word.getDifficulty(), request.timed());
        HangmanRoundModel round = new HangmanRoundModel();
        round.setPlaySession(session);
        round.setWord(word);
        round.setMaxErrors(MAX_ERRORS);
        round.setId_user_created(session.getUser().getId());
        round = rounds.save(round);

        return roundResponse(round, List.of(), null);
    }

    @Transactional(readOnly = true)
    public HangmanRoundResponse find(UUID roundId) {
        HangmanRoundModel round = ownedRound(roundId);
        ScoreResponse score = round.getPlaySession().isOpen() ? null
            : scores.findByPlaySession(round.getPlaySession().getId()).orElse(null);
        return roundResponse(round, guesses.findByRoundIdOrdered(round.getId()), score);
    }

    @Transactional
    public GuessResponse guess(UUID roundId, GuessRequest request) {
        HangmanRoundModel round = ownedRound(roundId);
        if (round.getResult() != null || !round.getPlaySession().isOpen()) {
            throw new ApiException(HttpStatus.CONFLICT, "HANGMAN_ROUND_FINISHED", "Partida já encerrada");
        }
        String letter = HangmanWord.normalizeLetter(request.letter());
        if (letter == null) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "HANGMAN_LETTER_INVALID", "Informe uma única letra de A a Z");
        }
        List<HangmanGuessModel> played = guesses.findByRoundIdOrdered(round.getId());
        if (played.stream().anyMatch(g -> g.getLetter().equals(letter))) {
            throw new ApiException(HttpStatus.CONFLICT, "HANGMAN_LETTER_ALREADY_USED", "Letra já usada");
        }

        String word = round.getWord().getText();
        boolean correct = word.indexOf(letter) >= 0;
        HangmanGuessModel guess = new HangmanGuessModel();
        guess.setRound(round);
        guess.setLetter(letter);
        guess.setCorrect(correct);
        guess.setId_user_created(round.getPlaySession().getUser().getId());
        played.add(guesses.save(guess));

        List<String> used = played.stream().map(HangmanGuessModel::getLetter).toList();
        List<String> revealed = played.stream().filter(HangmanGuessModel::isCorrect).map(HangmanGuessModel::getLetter).toList();
        int errors = used.size() - revealed.size();

        HangmanResult result = null;
        if (HangmanWord.isRevealed(word, revealed)) {
            result = HangmanResult.WIN;
        } else if (errors >= round.getMaxErrors()) {
            result = HangmanResult.LOSS;
        }

        ScoreResponse score = null;
        if (result != null) {
            round.setResult(result);
            rounds.save(round);
            score = sessions.finish(round.getPlaySession().getId()).score();
        }

        return new GuessResponse(correct, HangmanWord.mask(word, revealed), errors, round.getMaxErrors(), used,
            result != null, result, result != null ? word : null, score);
    }

    private static HangmanRoundResponse roundResponse(HangmanRoundModel round, List<HangmanGuessModel> played, ScoreResponse score) {
        String word = round.getWord().getText();
        List<String> used = played.stream().map(HangmanGuessModel::getLetter).toList();
        List<String> revealed = played.stream().filter(HangmanGuessModel::isCorrect).map(HangmanGuessModel::getLetter).toList();
        boolean finished = round.getResult() != null || !round.getPlaySession().isOpen();
        return new HangmanRoundResponse(round.getId(), round.getPlaySession().getId(), round.getWord().getCategory(),
            HangmanWord.mask(word, revealed), word.length(), used.size() - revealed.size(), round.getMaxErrors(), used,
            finished, round.getResult(), finished ? word : null, score);
    }

    private HangmanRoundModel ownedRound(UUID id) {
        HangmanRoundModel round = rounds.findActiveById(id)
            .orElseThrow(() -> new EntityNotFoundException("Partida " + id + " não encontrada"));
        sessions.requireOwner(round.getPlaySession());
        return round;
    }
}
