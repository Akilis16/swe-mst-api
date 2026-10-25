package com.project.mst.module.routes;

import java.util.Optional;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends BaseRepository<RouteModel> {
    Optional<RouteModel> findByUrl(String url);
}
