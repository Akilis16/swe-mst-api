package com.project.mst.module.tictactoe;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicTacToeRepository extends JpaRepository<TicTacToeModel, UUID> {}
