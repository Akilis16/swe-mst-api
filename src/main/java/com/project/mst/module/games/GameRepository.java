package com.project.mst.module.games;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<GameModel, UUID> {
    Optional<GameModel> findBySlug(String slug);
}
