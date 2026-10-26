package com.project.mst.module.users;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController extends CrudController<UserModel> {

    public UserController(UserService service) {
        super(service);
    }
}
