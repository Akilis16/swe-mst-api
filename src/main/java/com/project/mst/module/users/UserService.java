package com.project.mst.module.users;

import com.project.mst.infra.service.CrudService;

import org.springframework.stereotype.Service;

@Service
public class UserService extends CrudService<UserModel> {

    public UserService(UserRepository repository) {
        super(repository);
    }
}
