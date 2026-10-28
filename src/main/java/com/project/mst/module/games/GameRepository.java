package com.project.mst.module.games;

import java.util.List;
import java.util.Optional;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends BaseRepository<GameModel> {

    Optional<GameModel> findBySlug(String slug);

    @Query("select g from game g where g.deleted_at is null and g.active = true order by g.title")
    List<GameModel> findAllActiveAndEnabled();

    @Query("select g from game g where g.slug = ?1 and g.deleted_at is null and g.active = true")
    Optional<GameModel> findEnabledBySlug(String slug);
}
