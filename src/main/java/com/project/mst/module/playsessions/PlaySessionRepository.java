package com.project.mst.module.playsessions;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaySessionRepository extends JpaRepository<PlaySessionModel, UUID> {}
