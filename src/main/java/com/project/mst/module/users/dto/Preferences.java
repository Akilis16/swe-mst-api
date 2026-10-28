package com.project.mst.module.users.dto;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

public record Preferences(Boolean timed, Boolean highContrast, Boolean largeFont, Boolean reduceMotion) {

    public static final Preferences DEFAULTS = new Preferences(true, false, false, false);

    private static final ObjectMapper JSON = JsonMapper.builder().build();

    public Preferences merge(Preferences patch) {
        if (patch == null) {
            return this;
        }
        return new Preferences(
            patch.timed != null ? patch.timed : timed,
            patch.highContrast != null ? patch.highContrast : highContrast,
            patch.largeFont != null ? patch.largeFont : largeFont,
            patch.reduceMotion != null ? patch.reduceMotion : reduceMotion);
    }

    public static Preferences fromJson(String json) {
        if (json == null || json.isBlank()) {
            return DEFAULTS;
        }
        return DEFAULTS.merge(JSON.readValue(json, Preferences.class));
    }

    public String toJson() {
        return JSON.writeValueAsString(this);
    }
}
