package com.shani.caradvisor.workflow;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    List<MessageEntity> findByCaseIdOrderByIdAsc(String caseId);

    Optional<MessageEntity> findByCaseIdAndClientRequestId(String caseId, String clientRequestId);
}
