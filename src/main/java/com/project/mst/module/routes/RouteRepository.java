package com.project.mst.module.routes;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends JpaRepository<RouteModel, UUID> {
    Optional<RouteModel> findByUrl(String url);
}
