package com.shani.caradvisor.workflow;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<RepQuestionEntity, String> {

    List<RepQuestionEntity> findByCaseIdOrderByCreatedAtAsc(String caseId);

    List<RepQuestionEntity> findByStatusOrderByCreatedAtAsc(RepQuestionEntity.Status status);

    List<RepQuestionEntity> findByCaseIdAndFactKeyAndStatus(String caseId, FactKey factKey,
                                                           RepQuestionEntity.Status status);
}
