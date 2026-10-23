package com.project.mst.module.sudoku;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SudokuRepository extends JpaRepository<SudokuModel, UUID> {}
