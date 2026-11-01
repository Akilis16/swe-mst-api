package com.project.mst.module.quizzes;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.project.mst.infra.controller.ApiException;
import com.project.mst.module.playsessions.PlaySessionModel;
import com.project.mst.module.playsessions.PlaySessionService;
import com.project.mst.module.quizzes.dto.AnswerRequest;
import com.project.mst.module.quizzes.dto.AnswerResponse;
import com.project.mst.module.quizzes.dto.NextQuestionResponse;
import com.project.mst.module.quizzes.dto.OptionView;
import com.project.mst.module.quizzes.dto.QuizAttemptResponse;
import com.project.mst.module.quizzes.dto.QuizResultResponse;
import com.project.mst.module.quizzes.dto.StartAttemptRequest;
import com.project.mst.module.scores.ScoreService;
import com.project.mst.module.scores.dto.ScoreResponse;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuizPlayService {

    public static final String GAME_SLUG = "quiz";

    private final QuizRepository quizzes;
    private final QuestionRepository questions;
    private final OptionRepository options;
    private final QuizAttemptRepository attempts;
    private final QuizAnswerRepository answers;
    private final PlaySessionService sessions;
    private final ScoreService scores;

    public QuizPlayService(QuizRepository quizzes, QuestionRepository questions, OptionRepository options,
            QuizAttemptRepository attempts, QuizAnswerRepository answers, PlaySessionService sessions, ScoreService scores) {
        this.quizzes = quizzes;
        this.questions = questions;
        this.options = options;
        this.attempts = attempts;
        this.answers = answers;
        this.sessions = sessions;
        this.scores = scores;
    }

    @Transactional
    public QuizAttemptResponse start(StartAttemptRequest request) {
        QuizModel quiz = request.quizId() != null
            ? quizzes.findEnabledById(request.quizId())
                .orElseThrow(() -> new EntityNotFoundException("Quiz " + request.quizId() + " não encontrado"))
            : quizzes.findEnabledByGameSlug(GAME_SLUG).stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Nenhum quiz ativo"));
        List<QuestionModel> list = questions.findEnabledByQuizIdOrdered(quiz.getId());
        if (list.isEmpty()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "QUIZ_WITHOUT_QUESTIONS", "Quiz sem perguntas ativas");
        }

        PlaySessionModel session = sessions.open(GAME_SLUG, null, request.timed());
        QuizAttemptModel attempt = new QuizAttemptModel();
        attempt.setPlaySession(session);
        attempt.setQuiz(quiz);
        attempt.setCorrectCount(0);
        attempt.setId_user_created(session.getUser().getId());
        attempt = attempts.save(attempt);

        return new QuizAttemptResponse(attempt.getId(), session.getId(), quiz.getId(), quiz.getTitle(), list.size(), 0, 0, false);
    }

    @Transactional(readOnly = true)
    public QuizAttemptResponse find(UUID attemptId) {
        QuizAttemptModel attempt = ownedAttempt(attemptId);
        int total = questions.findEnabledByQuizIdOrdered(attempt.getQuiz().getId()).size();
        int answered = answers.findByAttemptId(attempt.getId()).size();
        return new QuizAttemptResponse(attempt.getId(), attempt.getPlaySession().getId(), attempt.getQuiz().getId(),
            attempt.getQuiz().getTitle(), total, answered, attempt.getCorrectCount(), !attempt.getPlaySession().isOpen());
    }

    @Transactional(readOnly = true)
    public NextQuestionResponse next(UUID attemptId) {
        QuizAttemptModel attempt = ownedAttempt(attemptId);
        requireOpen(attempt);
        List<QuestionModel> list = questions.findEnabledByQuizIdOrdered(attempt.getQuiz().getId());
        Set<UUID> done = answeredIds(attempt);
        for (int i = 0; i < list.size(); i++) {
            QuestionModel question = list.get(i);
            if (!done.contains(question.getId())) {
                List<OptionView> views = options.findActiveByQuestionId(question.getId()).stream().map(OptionView::from).toList();
                return new NextQuestionResponse(question.getId(), i + 1, list.size(), question.getCategory(), question.getText(), views);
            }
        }
        throw new ApiException(HttpStatus.NOT_FOUND, "QUIZ_NO_MORE_QUESTIONS", "Todas as perguntas já foram respondidas");
    }

    @Transactional
    public AnswerResponse answer(UUID attemptId, AnswerRequest request) {
        QuizAttemptModel attempt = ownedAttempt(attemptId);
        requireOpen(attempt);
        List<QuestionModel> list = questions.findEnabledByQuizIdOrdered(attempt.getQuiz().getId());
        QuestionModel question = list.stream().filter(q -> q.getId().equals(request.questionId())).findFirst()
            .orElseThrow(() -> new EntityNotFoundException("Pergunta " + request.questionId() + " não encontrada neste quiz"));
        Set<UUID> done = answeredIds(attempt);
        if (done.contains(question.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "QUIZ_QUESTION_ALREADY_ANSWERED", "Pergunta já respondida");
        }

        List<OptionModel> choices = options.findActiveByQuestionId(question.getId());
        OptionModel chosen = choices.stream().filter(o -> o.getId().equals(request.optionId())).findFirst()
            .orElseThrow(() -> new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "QUIZ_OPTION_INVALID", "Alternativa não pertence à pergunta"));
        OptionModel correct = choices.stream().filter(o -> Boolean.TRUE.equals(o.getIsCorrect())).findFirst().orElse(null);
        boolean isCorrect = Boolean.TRUE.equals(chosen.getIsCorrect());

        QuizAnswerModel answer = new QuizAnswerModel();
        answer.setAttempt(attempt);
        answer.setQuestion(question);
        answer.setOption(chosen);
        answer.setCorrect(isCorrect);
        answer.setAnsweredAt(new Date());
        answer.setId_user_created(attempt.getPlaySession().getUser().getId());
        answers.save(answer);

        if (isCorrect) {
            attempt.setCorrectCount(attempt.getCorrectCount() + 1);
            attempts.save(attempt);
        }

        return new AnswerResponse(isCorrect, correct == null ? null : correct.getId(), attempt.getCorrectCount(),
            done.size() + 1, list.size());
    }

    @Transactional
    public QuizResultResponse finish(UUID attemptId) {
        QuizAttemptModel attempt = ownedAttempt(attemptId);
        int total = questions.findEnabledByQuizIdOrdered(attempt.getQuiz().getId()).size();
        int correct = attempt.getCorrectCount();
        int performance = QuizPerformance.percent(correct, total);

        if (!attempt.getPlaySession().isOpen()) {
            ScoreResponse score = scores.findByPlaySession(attempt.getPlaySession().getId()).orElse(null);
            return new QuizResultResponse(correct, total, performance, QuizPerformance.message(performance), score);
        }
        int answered = answers.findByAttemptId(attempt.getId()).size();
        if (answered < total) {
            throw new ApiException(HttpStatus.CONFLICT, "QUIZ_QUESTIONS_PENDING", (total - answered) + " pergunta(s) ainda sem resposta");
        }
        ScoreResponse score = sessions.finish(attempt.getPlaySession().getId()).score();
        return new QuizResultResponse(correct, total, performance, QuizPerformance.message(performance), score);
    }

    private Set<UUID> answeredIds(QuizAttemptModel attempt) {
        return answers.findByAttemptId(attempt.getId()).stream()
            .map(a -> a.getQuestion().getId()).collect(Collectors.toSet());
    }

    private static void requireOpen(QuizAttemptModel attempt) {
        if (!attempt.getPlaySession().isOpen()) {
            throw new ApiException(HttpStatus.CONFLICT, "QUIZ_ATTEMPT_FINISHED", "Tentativa já encerrada");
        }
    }

    private QuizAttemptModel ownedAttempt(UUID id) {
        QuizAttemptModel attempt = attempts.findActiveById(id)
            .orElseThrow(() -> new EntityNotFoundException("Tentativa " + id + " não encontrada"));
        sessions.requireOwner(attempt.getPlaySession());
        return attempt;
    }
}
