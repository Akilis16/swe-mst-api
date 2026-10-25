package com.project.mst.module.profiles;

import java.util.Optional;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends BaseRepository<ProfileModel> {
    Optional<ProfileModel> findByName(String name);
}
