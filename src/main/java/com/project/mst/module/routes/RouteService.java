package com.project.mst.module.routes;

import com.project.mst.infra.service.CrudService;

import org.springframework.stereotype.Service;

@Service
public class RouteService extends CrudService<RouteModel> {

    public RouteService(RouteRepository repository) {
        super(repository);
    }
}
