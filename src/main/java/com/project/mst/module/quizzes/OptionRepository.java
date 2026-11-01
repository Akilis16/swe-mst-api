package com.project.mst.module.quizzes;

import java.util.List;
import java.util.UUID;

import com.project.mst.infra.database.repository.BaseRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OptionRepository extends BaseRepository<OptionModel> {

    @Query("select o from option o where o.question.id = ?1 and o.deleted_at is null order by o.created_at, o.id")
    List<OptionModel> findActiveByQuestionId(UUID questionId);
}
