package com.project.mst.module.scores.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.project.mst.module.scores.ScoreModel;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

public record ScoreResponse(UUID id, UUID playSessionId, int value, Map<String, Object> breakdown, Instant createdAt) {

    private static final ObjectMapper JSON = JsonMapper.builder().build();
    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() { };

    public static ScoreResponse from(ScoreModel score) {
        return new ScoreResponse(
            score.getId(),
            score.getPlaySession().getId(),
            score.getValue() == null ? 0 : score.getValue(),
            parseBreakdown(score.getBreakdown()),
            score.getCreated_at() == null ? null : score.getCreated_at().toInstant());
    }

    private static Map<String, Object> parseBreakdown(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        return JSON.readValue(json, MAP);
    }
}
