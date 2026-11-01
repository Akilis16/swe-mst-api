package com.project.mst.module.sudoku;

import java.util.List;

import com.project.mst.infra.database.repository.BaseRepository;
import com.project.mst.module.playsessions.Difficulty;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SudokuPuzzleRepository extends BaseRepository<SudokuPuzzleModel> {

    @Query("select p from sudoku_puzzle p where p.difficulty = ?1 and p.deleted_at is null and p.active = true")
    List<SudokuPuzzleModel> findEnabledByDifficulty(Difficulty difficulty);
}
