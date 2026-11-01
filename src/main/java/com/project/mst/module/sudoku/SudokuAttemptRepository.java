package com.project.mst.module.sudoku;

import java.util.Optional;
import java.util.UUID;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SudokuAttemptRepository extends BaseRepository<SudokuAttemptModel> {

    @Query("select a from sudoku_attempt a where a.playSession.id = ?1 and a.deleted_at is null")
    Optional<SudokuAttemptModel> findActiveByPlaySessionId(UUID playSessionId);
}
