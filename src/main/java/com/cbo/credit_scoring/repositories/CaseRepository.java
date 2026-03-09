package com.cbo.credit_scoring.repositories;

import com.cbo.credit_scoring.models.Case;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {
    Optional<Case> findByCaseId(String caseId);
    boolean existsByCaseId(String caseId);
}