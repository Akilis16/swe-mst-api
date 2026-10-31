package com.project.mst.module.tictactoe;

import java.util.Optional;
import java.util.UUID;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TicTacToeMatchRepository extends BaseRepository<TicTacToeMatchModel> {

    @Query("select m from tic_tac_toe_match m where m.playSession.id = ?1 and m.deleted_at is null")
    Optional<TicTacToeMatchModel> findActiveByPlaySessionId(UUID playSessionId);
}
