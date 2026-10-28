package com.project.mst.module.games.dto;

import java.util.UUID;

import com.project.mst.module.games.GameModel;

public record GameResponse(UUID id, String slug, String title, String description, String imagePath, boolean active) {

    public static GameResponse from(GameModel game) {
        return new GameResponse(
            game.getId(), game.getSlug(), game.getTitle(), game.getDescription(), game.getImagePath(), game.isActive());
    }
}
