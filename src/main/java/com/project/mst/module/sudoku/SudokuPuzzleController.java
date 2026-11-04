package com.project.mst.module.sudoku;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/admin/puzzles")
@Tag(name = "Conteúdo (ADMIN)")
public class SudokuPuzzleController extends CrudController<SudokuPuzzleModel> {

    public SudokuPuzzleController(SudokuPuzzleService service) {
        super(service);
    }
}
