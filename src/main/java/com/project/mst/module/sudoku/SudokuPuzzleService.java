package com.project.mst.module.sudoku;

import java.util.List;
import java.util.UUID;

import com.project.mst.infra.controller.ApiException;
import com.project.mst.infra.service.CrudService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SudokuPuzzleService extends CrudService<SudokuPuzzleModel> {

    public SudokuPuzzleService(SudokuPuzzleRepository repository) {
        super(repository);
    }

    @Override
    @Transactional
    public SudokuPuzzleModel create(SudokuPuzzleModel entity) {
        validate(entity);
        return super.create(entity);
    }

    @Override
    @Transactional
    public SudokuPuzzleModel update(UUID id, SudokuPuzzleModel entity) {
        validate(entity);
        return super.update(id, entity);
    }

    private static void validate(SudokuPuzzleModel puzzle) {
        List<String> problems = SudokuValidator.problems(puzzle.getGivens(), puzzle.getSolution());
        if (!problems.isEmpty()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "SUDOKU_PUZZLE_INVALID", String.join("; ", problems));
        }
    }
}
