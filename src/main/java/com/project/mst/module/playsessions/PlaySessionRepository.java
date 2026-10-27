package com.project.mst.module.playsessions;

import java.util.List;
import java.util.UUID;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaySessionRepository extends BaseRepository<PlaySessionModel> {

    @Query("select s from play_session s where s.user.id = ?1 and s.deleted_at is null order by s.started_at desc")
    List<PlaySessionModel> findAllActiveByUserId(UUID userId);
}
