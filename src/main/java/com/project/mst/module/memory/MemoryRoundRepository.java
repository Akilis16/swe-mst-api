package com.project.mst.module.memory;

import java.util.Optional;
import java.util.UUID;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MemoryRoundRepository extends BaseRepository<MemoryRoundModel> {

    @Query("select r from memory_round r where r.playSession.id = ?1 and r.deleted_at is null")
    Optional<MemoryRoundModel> findActiveByPlaySessionId(UUID playSessionId);
}
