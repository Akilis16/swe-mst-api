package com.project.mst.module.hangman;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import com.project.mst.infra.controller.ApiException;
import com.project.mst.module.games.GameModel;
import com.project.mst.module.hangman.dto.GuessRequest;
import com.project.mst.module.hangman.dto.GuessResponse;
import com.project.mst.module.hangman.dto.HangmanRoundResponse;
import com.project.mst.module.hangman.dto.StartHangmanRequest;
import com.project.mst.module.playsessions.Difficulty;
import com.project.mst.module.playsessions.PlaySessionModel;
import com.project.mst.module.playsessions.PlaySessionService;
import com.project.mst.module.playsessions.PlaySessionStatus;
import com.project.mst.module.playsessions.dto.PlaySessionResponse;
import com.project.mst.module.scores.ScoreService;
import com.project.mst.module.users.UserModel;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HangmanServiceTest {

    @Mock WordRepository words;
    @Mock HangmanRoundRepository rounds;
    @Mock HangmanGuessRepository guesses;
    @Mock PlaySessionService sessions;
    @Mock ScoreService scores;

    HangmanService service;
    WordModel word = new WordModel();
    HangmanRoundModel round = new HangmanRoundModel();
    PlaySessionModel session = new PlaySessionModel();
    List<HangmanGuessModel> stored = new ArrayList<>();

    @BeforeEach
    void setUp() {
        service = new HangmanService(words, rounds, guesses, sessions, scores, new Random(5));
        word.setId(UUID.randomUUID());
        word.setText("banana");
        word.setCategory("frutas");
        word.setDifficulty(Difficulty.MEDIUM);
        UserModel user = new UserModel();
        user.setId(UUID.randomUUID());
        GameModel game = new GameModel();
        game.setSlug("forca");
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setGame(game);
        session.setStatus(PlaySessionStatus.IN_PROGRESS);
        session.setStartedAt(new Date());
        round.setId(UUID.randomUUID());
        round.setWord(word);
        round.setPlaySession(session);
        round.setMaxErrors(6);
    }

    @Test
    void iniciarSorteiaPalavraDaCategoriaESoDevolveAMascara() {
        when(words.findEnabledByCategory("FRUTAS")).thenReturn(List.of(word));
        when(sessions.open("forca", Difficulty.MEDIUM, null)).thenReturn(session);
        when(rounds.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HangmanRoundResponse response = service.start(new StartHangmanRequest(" frutas ", null));

        assertThat(response.maskedWord()).isEqualTo("_ _ _ _ _ _");
        assertThat(response.length()).isEqualTo(6);
        assertThat(response.word()).isNull();
        assertThat(response.category()).isEqualTo("FRUTAS");
    }

    @Test
    void categoriaSemPalavrasRetorna404() {
        when(words.findEnabledByCategory("CORES")).thenReturn(List.of());

        assertThatThrownBy(() -> service.start(new StartHangmanRequest("cores", null)))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void letraPresenteRevelaTodasAsOcorrencias() {
        stubRound();

        GuessResponse response = service.guess(round.getId(), new GuessRequest("a"));

        assertThat(response.correct()).isTrue();
        assertThat(response.maskedWord()).isEqualTo("_ A _ A _ A");
        assertThat(response.errors()).isZero();
        assertThat(response.usedLetters()).containsExactly("A");
        assertThat(response.word()).isNull();
        verify(sessions, never()).finish(any());
    }

    @Test
    void letraRepetidaEInvalidaSaoRejeitadas() {
        stubRound();
        service.guess(round.getId(), new GuessRequest("B"));

        assertThatThrownBy(() -> service.guess(round.getId(), new GuessRequest("b")))
            .isInstanceOf(ApiException.class)
            .extracting(e -> ((ApiException) e).getCode()).isEqualTo("HANGMAN_LETTER_ALREADY_USED");
        assertThatThrownBy(() -> service.guess(round.getId(), new GuessRequest("ab")))
            .isInstanceOf(ApiException.class)
            .extracting(e -> ((ApiException) e).getCode()).isEqualTo("HANGMAN_LETTER_INVALID");
        assertThatThrownBy(() -> service.guess(round.getId(), new GuessRequest("ç")))
            .isInstanceOf(ApiException.class)
            .extracting(e -> ((ApiException) e).getCode()).isEqualTo("HANGMAN_LETTER_INVALID");
    }

    @Test
    void sextoErroEncerraComDerrotaERevelaAPalavra() {
        stubRound();
        when(sessions.finish(session.getId())).thenReturn(PlaySessionResponse.from(session, null));

        GuessResponse last = null;
        for (String letter : List.of("X", "Y", "Z", "W", "K", "Q")) {
            last = service.guess(round.getId(), new GuessRequest(letter));
        }

        assertThat(last.errors()).isEqualTo(6);
        assertThat(last.finished()).isTrue();
        assertThat(last.result()).isEqualTo(HangmanResult.LOSS);
        assertThat(last.word()).isEqualTo("BANANA");
        assertThat(round.getResult()).isEqualTo(HangmanResult.LOSS);

        assertThatThrownBy(() -> service.guess(round.getId(), new GuessRequest("A")))
            .isInstanceOf(ApiException.class)
            .extracting(e -> ((ApiException) e).getCode()).isEqualTo("HANGMAN_ROUND_FINISHED");
    }

    @Test
    void ultimaLetraEncerraComVitoria() {
        stubRound();
        when(sessions.finish(session.getId())).thenReturn(PlaySessionResponse.from(session, null));

        service.guess(round.getId(), new GuessRequest("B"));
        service.guess(round.getId(), new GuessRequest("N"));
        GuessResponse win = service.guess(round.getId(), new GuessRequest("A"));

        assertThat(win.result()).isEqualTo(HangmanResult.WIN);
        assertThat(win.maskedWord()).isEqualTo("B A N A N A");
        verify(sessions).finish(session.getId());
    }

    private void stubRound() {
        when(rounds.findActiveById(round.getId())).thenReturn(Optional.of(round));
        when(guesses.findByRoundIdOrdered(round.getId())).thenAnswer(inv -> new ArrayList<>(stored));
        when(guesses.save(any())).thenAnswer(inv -> {
            stored.add(inv.getArgument(0));
            return inv.getArgument(0);
        });
        lenient().when(rounds.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }
}
