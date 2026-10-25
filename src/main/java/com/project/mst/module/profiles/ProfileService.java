package com.project.mst.module.profiles;

import com.project.mst.infra.service.CrudService;

import org.springframework.stereotype.Service;

@Service
public class ProfileService extends CrudService<ProfileModel> {

    public ProfileService(ProfileRepository repository) {
        super(repository);
    }
}
