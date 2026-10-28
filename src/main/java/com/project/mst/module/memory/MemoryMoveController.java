package com.project.mst.module.memory;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/memory-moves")
public class MemoryMoveController extends CrudController<MemoryMoveModel> {

    public MemoryMoveController(MemoryMoveService service) {
        super(service);
    }
}
