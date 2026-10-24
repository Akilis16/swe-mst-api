package com.project.mst.module.profiles;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<ProfileModel, UUID> {
    Optional<ProfileModel> findByName(String name);
}
