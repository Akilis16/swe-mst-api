package com.project.mst.module.memory;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemoryMoveRepository extends JpaRepository<MemoryMoveModel, UUID> {}
