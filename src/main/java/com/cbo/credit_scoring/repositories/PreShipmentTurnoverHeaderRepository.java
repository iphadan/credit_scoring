package com.cbo.credit_scoring.repositories;

import com.cbo.credit_scoring.models.PreShipmentTurnoverHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PreShipmentTurnoverHeaderRepository extends JpaRepository<PreShipmentTurnoverHeader, Long> {

    // Find by caseId (unique identifier)
    Optional<PreShipmentTurnoverHeader> findByCaseId(String caseId);

    // Basic search methods
    List<PreShipmentTurnoverHeader> findByCustomerNameContainingIgnoreCase(String customerName);

    Optional<PreShipmentTurnoverHeader> findByAccountNumber(String accountNumber);

    List<PreShipmentTurnoverHeader> findByTypeOfFacility(String typeOfFacility);

    List<PreShipmentTurnoverHeader> findByIndustryType(String industryType);

    // Date range queries
    List<PreShipmentTurnoverHeader> findByDateApprovedBetween(LocalDate startDate, LocalDate endDate);

    List<PreShipmentTurnoverHeader> findByReportingDate(LocalDate reportingDate);


    // Custom search
    @Query("SELECT h FROM PreShipmentTurnoverHeader h WHERE " +
            "LOWER(h.customerName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "h.accountNumber LIKE CONCAT('%', :searchTerm, '%') OR " +
            "h.caseId LIKE CONCAT('%', :searchTerm, '%')")
    List<PreShipmentTurnoverHeader> searchByAnyField(@Param("searchTerm") String searchTerm);

    // Check existence
    boolean existsByCaseId(String caseId);

    boolean existsByAccountNumber(String accountNumber);

    // Find headers with turnover records
    @Query("SELECT DISTINCT h FROM PreShipmentTurnoverHeader h LEFT JOIN FETCH h.turnoverRecords WHERE h.id = :id")
    Optional<PreShipmentTurnoverHeader> findByIdWithTurnoverRecords(@Param("id") Long id);

    @Query("SELECT DISTINCT h FROM PreShipmentTurnoverHeader h LEFT JOIN FETCH h.turnoverRecords WHERE h.caseId = :caseId")
    Optional<PreShipmentTurnoverHeader> findByCaseIdWithTurnoverRecords(@Param("caseId") String caseId);
}