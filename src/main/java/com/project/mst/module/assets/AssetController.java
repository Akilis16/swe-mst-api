package com.project.mst.module.assets;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/assets")
public class AssetController extends CrudController<AssetModel> {

    public AssetController(AssetService service) {
        super(service);
    }
}
