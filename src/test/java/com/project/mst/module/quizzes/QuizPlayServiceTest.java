package com.project.mst.module.quizzes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.project.mst.infra.controller.ApiException;
import com.project.mst.module.games.GameModel;
import com.project.mst.module.playsessions.PlaySessionModel;
import com.project.mst.module.playsessions.PlaySessionService;
import com.project.mst.module.playsessions.PlaySessionStatus;
import com.project.mst.module.playsessions.dto.PlaySessionResponse;
import com.project.mst.module.quizzes.dto.AnswerRequest;
import com.project.mst.module.quizzes.dto.AnswerResponse;
import com.project.mst.module.quizzes.dto.NextQuestionResponse;
import com.project.mst.module.quizzes.dto.QuizResultResponse;
import com.project.mst.module.scores.ScoreService;
import com.project.mst.module.users.UserModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuizPlayServiceTest {

    @Mock QuizRepository quizzes;
    @Mock QuestionRepository questions;
    @Mock OptionRepository options;
    @Mock QuizAttemptRepository attempts;
    @Mock QuizAnswerRepository answers;
    @Mock PlaySessionService sessions;
    @Mock ScoreService scores;

    QuizPlayService service;
    QuizModel quiz = new QuizModel();
    QuizAttemptModel attempt = new QuizAttemptModel();
    PlaySessionModel session = new PlaySessionModel();
    List<QuestionModel> list = new ArrayList<>();
    List<QuizAnswerModel> given = new ArrayList<>();

    @BeforeEach
    void setUp() {
        service = new QuizPlayService(quizzes, questions, options, attempts, answers, sessions, scores);
        quiz.setId(UUID.randomUUID());
        quiz.setTitle("Quiz");
        UserModel user = new UserModel();
        user.setId(UUID.randomUUID());
        GameModel game = new GameModel();
        game.setSlug("quiz");
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setGame(game);
        session.setStatus(PlaySessionStatus.IN_PROGRESS);
        session.setStartedAt(new Date());
        attempt.setId(UUID.randomUUID());
        attempt.setQuiz(quiz);
        attempt.setPlaySession(session);
        attempt.setCorrectCount(0);
        for (int i = 0; i < 2; i++) {
            QuestionModel question = new QuestionModel();
            question.setId(UUID.randomUUID());
            question.setQuiz(quiz);
            question.setText("Pergunta " + i);
            question.setPosition(i + 1);
            list.add(question);
        }
    }

    @Test
    void proximaPerguntaVemSemGabarito() {
        stubAttempt();
        List<OptionModel> choices = optionsFor(list.get(0));
        when(options.findActiveByQuestionId(list.get(0).getId())).thenReturn(choices);

        NextQuestionResponse next = service.next(attempt.getId());

        assertThat(next.index()).isEqualTo(1);
        assertThat(next.total()).isEqualTo(2);
        assertThat(next.options()).hasSize(4).extracting("text").containsExactly("A", "B", "C", "D");
    }

    @Test
    void responderCorrigeNoServidorEDevolveACorreta() {
        stubAttempt();
        List<OptionModel> choices = optionsFor(list.get(0));
        when(options.findActiveByQuestionId(list.get(0).getId())).thenReturn(choices);
        when(answers.save(any())).thenAnswer(inv -> {
            given.add(inv.getArgument(0));
            return inv.getArgument(0);
        });

        AnswerResponse wrong = service.answer(attempt.getId(), new AnswerRequest(list.get(0).getId(), choices.get(0).getId()));

        assertThat(wrong.correct()).isFalse();
        assertThat(wrong.correctOptionId()).isEqualTo(choices.get(2).getId());
        assertThat(wrong.correctCount()).isZero();
        assertThat(wrong.answered()).isEqualTo(1);

        assertThatThrownBy(() -> service.answer(attempt.getId(), new AnswerRequest(list.get(0).getId(), choices.get(2).getId())))
            .isInstanceOf(ApiException.class)
            .extracting(e -> ((ApiException) e).getCode()).isEqualTo("QUIZ_QUESTION_ALREADY_ANSWERED");
    }

    @Test
    void alternativaDeOutraPerguntaRetorna422() {
        stubAttempt();
        when(options.findActiveByQuestionId(list.get(0).getId())).thenReturn(optionsFor(list.get(0)));

        assertThatThrownBy(() -> service.answer(attempt.getId(), new AnswerRequest(list.get(0).getId(), UUID.randomUUID())))
            .isInstanceOf(ApiException.class)
            .extracting(e -> ((ApiException) e).getCode()).isEqualTo("QUIZ_OPTION_INVALID");
    }

    @Test
    void encerrarExigeTodasAsRespostasECalculaDesempenho() {
        stubAttempt();
        assertThatThrownBy(() -> service.finish(attempt.getId()))
            .isInstanceOf(ApiException.class)
            .extracting(e -> ((ApiException) e).getCode()).isEqualTo("QUIZ_QUESTIONS_PENDING");

        for (QuestionModel question : list) {
            QuizAnswerModel answer = new QuizAnswerModel();
            answer.setQuestion(question);
            given.add(answer);
        }
        attempt.setCorrectCount(1);
        when(sessions.finish(session.getId())).thenReturn(PlaySessionResponse.from(session, null));

        QuizResultResponse result = service.finish(attempt.getId());

        assertThat(result.correctCount()).isEqualTo(1);
        assertThat(result.total()).isEqualTo(2);
        assertThat(result.performance()).isEqualTo(50);
        assertThat(result.message()).isEqualTo("Bom");
    }

    @Test
    void faixasDeDesempenhoSeguemALegenda() {
        assertThat(QuizPerformance.percent(28, 28)).isEqualTo(100);
        assertThat(QuizPerformance.percent(25, 28)).isEqualTo(89);
        assertThat(QuizPerformance.message(90)).isEqualTo("Excelente");
        assertThat(QuizPerformance.message(89)).isEqualTo("Muito bom");
        assertThat(QuizPerformance.message(70)).isEqualTo("Muito bom");
        assertThat(QuizPerformance.message(50)).isEqualTo("Bom");
        assertThat(QuizPerformance.message(49)).isEqualTo("Pode melhorar");
    }

    private void stubAttempt() {
        when(attempts.findActiveById(attempt.getId())).thenReturn(Optional.of(attempt));
        when(questions.findEnabledByQuizIdOrdered(quiz.getId())).thenReturn(list);
        when(answers.findByAttemptId(attempt.getId())).thenAnswer(inv -> new ArrayList<>(given));
    }

    private static List<OptionModel> optionsFor(QuestionModel question) {
        List<OptionModel> choices = new ArrayList<>();
        for (String text : List.of("A", "B", "C", "D")) {
            OptionModel option = new OptionModel();
            option.setId(UUID.randomUUID());
            option.setQuestion(question);
            option.setText(text);
            option.setIsCorrect(text.equals("C"));
            choices.add(option);
        }
        return choices;
    }
}
