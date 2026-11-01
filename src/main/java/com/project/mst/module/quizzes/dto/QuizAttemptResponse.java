package com.project.mst.module.quizzes.dto;

import java.util.UUID;

public record QuizAttemptResponse(UUID attemptId, UUID playSessionId, UUID quizId, String title,
    int totalQuestions, int answered, int correctCount, boolean finished) {
}
