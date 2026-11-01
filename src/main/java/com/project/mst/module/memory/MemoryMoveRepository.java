package com.project.mst.module.memory;

import java.util.List;
import java.util.UUID;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MemoryMoveRepository extends BaseRepository<MemoryMoveModel> {

    @Query("select m from memory_move m where m.round.id = ?1 and m.deleted_at is null order by m.moveNumber")
    List<MemoryMoveModel> findByRoundIdOrdered(UUID roundId);
}
