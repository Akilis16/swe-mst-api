package com.project.mst.module.sudoku;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface SudokuRepository extends BaseRepository<SudokuModel> {}
