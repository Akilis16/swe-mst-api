package com.project.mst.module.sudoku;

import java.util.List;
import java.util.UUID;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SudokuEntryRepository extends BaseRepository<SudokuEntryModel> {

    @Query("select e from sudoku_entry e where e.attempt.id = ?1 and e.deleted_at is null order by e.enteredAt, e.created_at")
    List<SudokuEntryModel> findByAttemptIdOrdered(UUID attemptId);
}
