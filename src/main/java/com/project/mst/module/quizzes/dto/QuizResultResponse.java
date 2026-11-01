package com.project.mst.module.quizzes.dto;

import com.project.mst.module.scores.dto.ScoreResponse;

public record QuizResultResponse(int correctCount, int total, int performance, String message, ScoreResponse score) {
}
