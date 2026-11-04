package com.project.mst.module.users;

import com.project.mst.infra.controller.CrudController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/users")
@Tag(name = "Usuários (ADMIN)", description = "Administração de usuários e seus perfis")
public class UserController extends CrudController<UserModel> {

    public UserController(UserService service) {
        super(service);
    }
}
