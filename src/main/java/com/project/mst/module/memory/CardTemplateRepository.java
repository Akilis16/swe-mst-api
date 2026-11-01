package com.project.mst.module.memory;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CardTemplateRepository extends BaseRepository<CardTemplateModel> {

    @Query("select c from card_template c where c.theme.id = ?1 and c.deleted_at is null and c.active = true order by c.label")
    List<CardTemplateModel> findEnabledByThemeId(UUID themeId);

    @Query("select c from card_template c where c.id in ?1")
    List<CardTemplateModel> findAllByIds(Collection<UUID> ids);
}
