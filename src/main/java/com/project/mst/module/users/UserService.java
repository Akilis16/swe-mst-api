package com.project.mst.module.users;

import java.util.UUID;

import com.project.mst.infra.service.CrudService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService extends CrudService<UserModel> {

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        super(repository);
        this.passwordEncoder = passwordEncoder;
    }

    /** A senha nunca é gravada em texto puro, mesmo pelo CRUD de /users. */
    @Override
    @Transactional
    public UserModel create(UserModel entity) {
        entity.setPassword(hashIfPresent(entity.getPassword()));
        return super.create(entity);
    }

    /** Senha ausente na atualização mantém o hash atual; senha presente é re-hasheada. */
    @Override
    @Transactional
    public UserModel update(UUID id, UserModel entity) {
        UserModel current = findById(id);
        String password = entity.getPassword();

        if (password == null || password.isBlank()) {
            entity.setPassword(current.getPassword());
        } else {
            entity.setPassword(passwordEncoder.encode(password));
        }

        return super.update(id, entity);
    }

    private String hashIfPresent(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            return null;
        }
        return passwordEncoder.encode(rawPassword);
    }
}
