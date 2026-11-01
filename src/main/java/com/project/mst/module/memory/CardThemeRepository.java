package com.project.mst.module.memory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CardThemeRepository extends BaseRepository<CardThemeModel> {

    @Query("select t from card_theme t where t.deleted_at is null and t.active = true order by t.name")
    List<CardThemeModel> findAllActiveAndEnabled();

    @Query("select t from card_theme t where t.id = ?1 and t.deleted_at is null and t.active = true")
    Optional<CardThemeModel> findEnabledById(UUID id);
}
