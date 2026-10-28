package com.project.mst.module.sudoku;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sudoku")
public class SudokuController extends CrudController<SudokuModel> {

    public SudokuController(SudokuService service) {
        super(service);
    }
}
