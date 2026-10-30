package com.project.mst.module.sudoku;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/puzzles")
public class SudokuPuzzleController extends CrudController<SudokuPuzzleModel> {

    public SudokuPuzzleController(SudokuPuzzleService service) {
        super(service);
    }
}
