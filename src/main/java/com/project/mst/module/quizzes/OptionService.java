package com.project.mst.module.quizzes;

import com.project.mst.infra.service.CrudService;

import org.springframework.stereotype.Service;

@Service
public class OptionService extends CrudService<OptionModel> {

    public OptionService(OptionRepository repository) {
        super(repository);
    }
}
