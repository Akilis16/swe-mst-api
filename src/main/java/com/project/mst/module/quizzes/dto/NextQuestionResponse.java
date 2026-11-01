package com.project.mst.module.quizzes.dto;

import java.util.List;
import java.util.UUID;

public record NextQuestionResponse(UUID questionId, int index, int total, String category, String text, List<OptionView> options) {
}
