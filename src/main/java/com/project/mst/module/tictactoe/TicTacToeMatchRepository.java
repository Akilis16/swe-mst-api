package com.project.mst.module.tictactoe;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface TicTacToeMatchRepository extends BaseRepository<TicTacToeMatchModel> {
}
