package com.project.mst.module.memory;

import com.project.mst.infra.service.CrudService;

import org.springframework.stereotype.Service;

@Service
public class MemoryMoveService extends CrudService<MemoryMoveModel> {

    public MemoryMoveService(MemoryMoveRepository repository) {
        super(repository);
    }
}
