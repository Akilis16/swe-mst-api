package com.project.mst.module.quizzes;

import java.util.LinkedHashMap;
import java.util.Map;

import com.project.mst.module.playsessions.PlaySessionModel;
import com.project.mst.module.playsessions.PlaySessionService;
import com.project.mst.module.scores.ScoreCalculator;
import com.project.mst.module.scores.ScoreResult;

import org.springframework.stereotype.Component;

@Component
public class QuizScoreCalculator implements ScoreCalculator {

    private final QuizAttemptRepository attempts;
    private final QuestionRepository questions;

    public QuizScoreCalculator(QuizAttemptRepository attempts, QuestionRepository questions) {
        this.attempts = attempts;
        this.questions = questions;
    }

    @Override
    public String gameSlug() {
        return QuizPlayService.GAME_SLUG;
    }

    @Override
    public ScoreResult calculate(PlaySessionModel session) {
        QuizAttemptModel attempt = attempts.findActiveByPlaySessionId(session.getId()).orElse(null);
        if (attempt == null) {
            return ScoreResult.of(0);
        }
        int total = questions.findEnabledByQuizIdOrdered(attempt.getQuiz().getId()).size();
        int correct = attempt.getCorrectCount() == null ? 0 : attempt.getCorrectCount();
        int performance = QuizPerformance.percent(correct, total);

        Map<String, Object> breakdown = new LinkedHashMap<>();
        breakdown.put("correct", correct);
        breakdown.put("errors", Math.max(0, total - correct));
        breakdown.put("total", total);
        breakdown.put("performance", performance);
        breakdown.put("message", QuizPerformance.message(performance));
        breakdown.put("seconds", PlaySessionService.elapsedSeconds(session));
        return new ScoreResult(performance, breakdown);
    }
}
