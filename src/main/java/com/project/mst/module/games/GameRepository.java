package com.project.mst.module.games;

import java.util.Optional;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends BaseRepository<GameModel> {
    Optional<GameModel> findBySlug(String slug);
}
