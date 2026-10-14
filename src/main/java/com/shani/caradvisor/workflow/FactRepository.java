package com.shani.caradvisor.workflow;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FactRepository extends JpaRepository<FactEntity, Long> {

    List<FactEntity> findByCaseId(String caseId);

    Optional<FactEntity> findByCaseIdAndFactKey(String caseId, FactKey factKey);
}
