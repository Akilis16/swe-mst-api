package com.project.mst.module.scores;

import java.util.Map;

public record ScoreResult(int value, Map<String, Object> breakdown) {

    public static ScoreResult of(int value) {
        return new ScoreResult(value, Map.of());
    }
}
