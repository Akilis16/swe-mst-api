package com.project.mst.infra.database.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.project.mst.infra.database.model.Model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<T extends Model> extends JpaRepository<T, UUID> {
    @Query("select e from #{#entityName} e where e.deleted_at is null")
    List<T> findAllActive();

    @Query("select e from #{#entityName} e where e.id = ?1 and e.deleted_at is null")
    Optional<T> findActiveById(UUID id);
}
