package com.project.mst.module.memory;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardTemplateRepository extends JpaRepository<CardTemplateModel, UUID> {}
