package com.project.mst.module.hangman;

import java.util.List;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WordRepository extends BaseRepository<WordModel> {

    @Query("select w from word w where w.category = ?1 and w.deleted_at is null and w.active = true")
    List<WordModel> findEnabledByCategory(String category);

    @Query("select w.category, count(w) from word w where w.deleted_at is null and w.active = true group by w.category order by w.category")
    List<Object[]> countEnabledByCategory();
}
