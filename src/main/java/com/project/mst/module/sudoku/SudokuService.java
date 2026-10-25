package com.project.mst.module.sudoku;

import com.project.mst.infra.service.CrudService;

import org.springframework.stereotype.Service;

@Service
public class SudokuService extends CrudService<SudokuModel> {

    public SudokuService(SudokuRepository repository) {
        super(repository);
    }
}
