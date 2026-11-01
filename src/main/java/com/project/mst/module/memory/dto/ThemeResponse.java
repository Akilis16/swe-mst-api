package com.project.mst.module.memory.dto;

import java.util.UUID;

import com.project.mst.module.memory.CardThemeModel;

public record ThemeResponse(UUID id, String name, String backImagePath) {

    public static ThemeResponse from(CardThemeModel theme) {
        return new ThemeResponse(theme.getId(), theme.getName(), theme.getBackImagePath());
    }
}
