package com.project.mst.module.sudoku;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sudoku-attempts")
public class SudokuAttemptController extends CrudController<SudokuAttemptModel> {

    public SudokuAttemptController(SudokuAttemptService service) {
        super(service);
    }
}
