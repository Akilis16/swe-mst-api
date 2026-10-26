package com.project.mst.module.profiles;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profiles")
public class ProfileController extends CrudController<ProfileModel> {

    public ProfileController(ProfileService service) {
        super(service);
    }
}
