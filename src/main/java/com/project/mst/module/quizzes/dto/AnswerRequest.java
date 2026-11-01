package com.project.mst.module.quizzes.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AnswerRequest(@NotNull UUID questionId, @NotNull UUID optionId) {
}
