package com.project.mst.module.routes;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/routes")
public class RouteController extends CrudController<RouteModel> {

    public RouteController(RouteService service) {
        super(service);
    }
}
