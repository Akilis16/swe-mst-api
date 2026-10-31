package com.project.mst.module.tictactoe;

import java.util.List;
import java.util.UUID;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TicTacToeMoveRepository extends BaseRepository<TicTacToeMoveModel> {

    @Query("select m from tic_tac_toe_move m where m.match.id = ?1 and m.deleted_at is null order by m.moveNumber")
    List<TicTacToeMoveModel> findByMatchIdOrdered(UUID matchId);
}
