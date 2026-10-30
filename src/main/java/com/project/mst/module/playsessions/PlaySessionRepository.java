package com.project.mst.module.playsessions;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaySessionRepository extends BaseRepository<PlaySessionModel> {

    @Query("select s from play_session s where s.deleted_at is null and s.user.id = ?1")
    Page<PlaySessionModel> findPageByUserId(UUID userId, Pageable pageable);

    @Query("select s from play_session s where s.deleted_at is null and s.user.id = ?1 and s.game.slug = ?2")
    Page<PlaySessionModel> findPageByUserIdAndGameSlug(UUID userId, String gameSlug, Pageable pageable);

    @Query("select s from play_session s where s.deleted_at is null")
    Page<PlaySessionModel> findPageAll(Pageable pageable);

    @Query("select s from play_session s where s.deleted_at is null and s.game.slug = ?1")
    Page<PlaySessionModel> findPageByGameSlug(String gameSlug, Pageable pageable);

    @Query("select s from play_session s join fetch s.game where s.deleted_at is null and s.user.id = ?1 and s.startedAt >= ?2 and s.startedAt < ?3 order by s.startedAt")
    List<PlaySessionModel> findByUserIdAndPeriod(UUID userId, Date from, Date to);

    @Query("select s from play_session s join fetch s.game where s.deleted_at is null and s.user.id = ?1 and s.game.slug = ?2 and s.startedAt >= ?3 and s.startedAt < ?4 order by s.startedAt")
    List<PlaySessionModel> findByUserIdAndGameSlugAndPeriod(UUID userId, String gameSlug, Date from, Date to);
}
