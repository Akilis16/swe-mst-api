package com.project.mst.module.quizzes.dto;

import java.util.UUID;

public record StartAttemptRequest(UUID quizId, Boolean timed) {
}
