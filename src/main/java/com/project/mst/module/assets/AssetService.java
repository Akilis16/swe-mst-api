package com.project.mst.module.assets;

import com.project.mst.infra.service.CrudService;

import org.springframework.stereotype.Service;

@Service
public class AssetService extends CrudService<AssetModel> {

    public AssetService(AssetRepository repository) {
        super(repository);
    }
}
