package com.project.mst.module.quizzes.dto;

import java.util.UUID;

public record AnswerResponse(boolean correct, UUID correctOptionId, int correctCount, int answered, int total) {
}
