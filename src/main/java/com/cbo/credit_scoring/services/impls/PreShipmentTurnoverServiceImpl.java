package com.cbo.credit_scoring.services.impls;

import com.cbo.credit_scoring.dtos.PreShipmentTurnoverDTO;
import com.cbo.credit_scoring.models.PreShipmentTurnover;
import com.cbo.credit_scoring.models.PreShipmentTurnoverHeader;
import com.cbo.credit_scoring.repositories.PreShipmentTurnoverRepository;
import com.cbo.credit_scoring.repositories.PreShipmentTurnoverHeaderRepository;
import com.cbo.credit_scoring.services.PreShipmentTurnoverService;
import com.cbo.credit_scoring.exceptions.ResourceNotFoundException;
import com.cbo.credit_scoring.exceptions.BadRequestException;
import com.cbo.credit_scoring.exceptions.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PreShipmentTurnoverServiceImpl implements PreShipmentTurnoverService {

    private final PreShipmentTurnoverRepository turnoverRepository;
    private final PreShipmentTurnoverHeaderRepository headerRepository;

    @Override
    public PreShipmentTurnoverDTO createTurnover(PreShipmentTurnoverDTO turnoverDTO) {
        log.info("Creating new pre-shipment turnover record");

        if (turnoverDTO.getHeaderId() == null) {
            throw new BadRequestException("Header ID is required");
        }

        // Find header
        PreShipmentTurnoverHeader header = headerRepository.findById(turnoverDTO.getHeaderId())
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + turnoverDTO.getHeaderId()));

        // Validate turnover data
        validateTurnover(turnoverDTO);

        // Check for duplicate month in same header
        if (turnoverDTO.getMonth() != null) {
            boolean monthExists = header.getTurnoverRecords().stream()
                    .anyMatch(t -> t.getMonth() != null && t.getMonth().equals(turnoverDTO.getMonth()));

            if (monthExists) {
                throw new DuplicateResourceException("Turnover record for month " + turnoverDTO.getMonth() + " already exists in this header");
            }
        }

        // Create new turnover record
        PreShipmentTurnover turnover = new PreShipmentTurnover();
        turnover.setMonth(turnoverDTO.getMonth());
        turnover.setDebitDisbursements(turnoverDTO.getDebitDisbursements() != null ?
                turnoverDTO.getDebitDisbursements() : BigDecimal.ZERO);
        turnover.setCreditPrincipalRepayments(turnoverDTO.getCreditPrincipalRepayments() != null ?
                turnoverDTO.getCreditPrincipalRepayments() : BigDecimal.ZERO);
        turnover.setHeader(header);

        // Save turnover
        PreShipmentTurnover savedTurnover = turnoverRepository.save(turnover);
        log.info("Turnover record created successfully with ID: {}", savedTurnover.getId());

        return mapToDTO(savedTurnover);
    }

    @Override
    public PreShipmentTurnoverDTO updateTurnover(Long id, PreShipmentTurnoverDTO turnoverDTO) {
        log.info("Updating turnover record with ID: {}", id);

        // Find existing turnover
        PreShipmentTurnover existingTurnover = turnoverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turnover record not found with ID: " + id));

        // Validate turnover data
        validateTurnover(turnoverDTO);

        // Check for duplicate month if month is being changed
        if (turnoverDTO.getMonth() != null && !turnoverDTO.getMonth().equals(existingTurnover.getMonth())) {
            boolean monthExists = existingTurnover.getHeader().getTurnoverRecords().stream()
                    .filter(t -> !t.getId().equals(id))
                    .anyMatch(t -> t.getMonth() != null && t.getMonth().equals(turnoverDTO.getMonth()));

            if (monthExists) {
                throw new DuplicateResourceException("Turnover record for month " + turnoverDTO.getMonth() + " already exists in this header");
            }
        }

        // Update fields
        if (turnoverDTO.getMonth() != null) existingTurnover.setMonth(turnoverDTO.getMonth());
        if (turnoverDTO.getDebitDisbursements() != null) existingTurnover.setDebitDisbursements(turnoverDTO.getDebitDisbursements());
        if (turnoverDTO.getCreditPrincipalRepayments() != null) existingTurnover.setCreditPrincipalRepayments(turnoverDTO.getCreditPrincipalRepayments());

        // Save updated turnover
        PreShipmentTurnover updatedTurnover = turnoverRepository.save(existingTurnover);
        log.info("Turnover record updated successfully with ID: {}", updatedTurnover.getId());

        return mapToDTO(updatedTurnover);
    }

    @Override
    public PreShipmentTurnoverDTO getTurnoverById(Long id) {
        log.info("Fetching turnover record with ID: {}", id);

        PreShipmentTurnover turnover = turnoverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turnover record not found with ID: " + id));

        return mapToDTO(turnover);
    }

    @Override
    public Page<PreShipmentTurnoverDTO> getAllTurnovers(Pageable pageable) {
        log.info("Fetching all turnover records with pagination");

        return turnoverRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Override
    public void deleteTurnover(Long id) {
        log.info("Deleting turnover record with ID: {}", id);

        PreShipmentTurnover turnover = turnoverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turnover record not found with ID: " + id));

        turnoverRepository.delete(turnover);
        log.info("Turnover record deleted successfully with ID: {}", id);
    }

    @Override
    public List<PreShipmentTurnoverDTO> getTurnoversByHeaderId(Long headerId) {
        log.info("Fetching turnover records for header ID: {}", headerId);

        if (!headerRepository.existsById(headerId)) {
            throw new ResourceNotFoundException("Header not found with ID: " + headerId);
        }

        return turnoverRepository.findByHeaderId(headerId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreShipmentTurnoverDTO> getTurnoversByMonth(YearMonth month) {
        log.info("Fetching turnover records for month: {}", month);

        return turnoverRepository.findByMonth(month)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PreShipmentTurnoverDTO> getTurnoversByMonthRange(YearMonth startMonth, YearMonth endMonth) {
        log.info("Fetching turnover records between {} and {}", startMonth, endMonth);

        return turnoverRepository.findByMonthBetween(startMonth, endMonth)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PreShipmentTurnoverDTO getTurnoverByHeaderIdAndMonth(Long headerId, YearMonth month) {
        log.info("Fetching turnover record for header ID: {} and month: {}", headerId, month);

        PreShipmentTurnover turnover = turnoverRepository.findByHeaderIdAndMonth(headerId, month)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Turnover record not found for header ID: " + headerId + " and month: " + month));

        return mapToDTO(turnover);
    }

    @Override
    public Map<String, Object> getTurnoverSummaryByHeader(Long headerId) {
        log.info("Generating turnover summary for header ID: {}", headerId);

        BigDecimal totalDebit = turnoverRepository.sumDebitByHeaderId(headerId);
        BigDecimal totalCredit = turnoverRepository.sumCreditByHeaderId(headerId);
        Long recordCount = turnoverRepository.countByHeaderId(headerId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("headerId", headerId);
        summary.put("totalDebit", totalDebit);
        summary.put("totalCredit", totalCredit);
        summary.put("netTurnover", totalDebit.subtract(totalCredit));
        summary.put("recordCount", recordCount);

        return summary;
    }

    @Override
    public Map<YearMonth, BigDecimal> getMonthlyDebitTrend(Long headerId) {
        log.info("Generating monthly debit trend for header ID: {}", headerId);

        List<PreShipmentTurnover> turnovers = turnoverRepository.findByHeaderId(headerId);

        return turnovers.stream()
                .filter(t -> t.getMonth() != null)
                .collect(Collectors.toMap(
                        PreShipmentTurnover::getMonth,
                        PreShipmentTurnover::getDebitDisbursements,
                        (v1, v2) -> v1.add(v2),
                        TreeMap::new
                ));
    }

    @Override
    public Map<YearMonth, BigDecimal> getMonthlyCreditTrend(Long headerId) {
        log.info("Generating monthly credit trend for header ID: {}", headerId);

        List<PreShipmentTurnover> turnovers = turnoverRepository.findByHeaderId(headerId);

        return turnovers.stream()
                .filter(t -> t.getMonth() != null)
                .collect(Collectors.toMap(
                        PreShipmentTurnover::getMonth,
                        PreShipmentTurnover::getCreditPrincipalRepayments,
                        (v1, v2) -> v1.add(v2),
                        TreeMap::new
                ));
    }

    // Helper methods
    private PreShipmentTurnoverDTO mapToDTO(PreShipmentTurnover turnover) {
        return PreShipmentTurnoverDTO.builder()
                .id(turnover.getId())
                .month(turnover.getMonth())
                .debitDisbursements(turnover.getDebitDisbursements())
                .creditPrincipalRepayments(turnover.getCreditPrincipalRepayments())
                .headerId(turnover.getHeader() != null ? turnover.getHeader().getId() : null)
                .build();
    }

    private void validateTurnover(PreShipmentTurnoverDTO dto) {
        if (dto.getDebitDisbursements() != null && dto.getDebitDisbursements().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Debit disbursements cannot be negative");
        }
        if (dto.getCreditPrincipalRepayments() != null && dto.getCreditPrincipalRepayments().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Credit principal repayments cannot be negative");
        }
    }
}