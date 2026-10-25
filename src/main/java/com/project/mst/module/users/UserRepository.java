package com.project.mst.module.users;

import java.util.Optional;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends BaseRepository<UserModel> {
    Optional<UserModel> findByEmail(String email);
}
