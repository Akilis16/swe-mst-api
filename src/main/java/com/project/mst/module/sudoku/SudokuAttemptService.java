package com.project.mst.module.sudoku;

import com.project.mst.infra.service.CrudService;

import org.springframework.stereotype.Service;

@Service
public class SudokuAttemptService extends CrudService<SudokuAttemptModel> {

    public SudokuAttemptService(SudokuAttemptRepository repository) {
        super(repository);
    }
}
