package com.cbo.credit_scoring.services.impls;

import com.cbo.credit_scoring.dtos.OdTurnoverDTO;
import com.cbo.credit_scoring.models.OdTurnover;
import com.cbo.credit_scoring.models.OdTurnoverHeader;
import com.cbo.credit_scoring.repositories.OdTurnoverRepository;
import com.cbo.credit_scoring.repositories.OdTurnoverHeaderRepository;
import com.cbo.credit_scoring.services.OdTurnoverService;
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
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OdTurnoverServiceImpl implements OdTurnoverService {

    private final OdTurnoverRepository turnoverRepository;
    private final OdTurnoverHeaderRepository headerRepository;

    @Override
    public OdTurnoverDTO createTurnover(OdTurnoverDTO turnoverDTO) {
        log.info("Creating new OD turnover record");

        if (turnoverDTO.getHeaderId() == null) {
            throw new BadRequestException("Header ID is required");
        }

        // Find header
        OdTurnoverHeader header = headerRepository.findById(turnoverDTO.getHeaderId())
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + turnoverDTO.getHeaderId()));

        // Validate turnover data
        validateTurnover(turnoverDTO);

        // Check for duplicate month
        if (turnoverDTO.getMonth() != null) {
            boolean monthExists = header.getTurnoverRecords().stream()
                    .anyMatch(t -> t.getMonth() != null && t.getMonth().equals(turnoverDTO.getMonth()));

            if (monthExists) {
                throw new DuplicateResourceException("Turnover record for month " + turnoverDTO.getMonth() + " already exists");
            }
        }

        // Create new turnover record
        OdTurnover turnover = new OdTurnover();
        turnover.setMonth(turnoverDTO.getMonth());
        turnover.setTotalTurnoverCredit(turnoverDTO.getTotalTurnoverCredit() != null ?
                turnoverDTO.getTotalTurnoverCredit() : BigDecimal.ZERO);
        turnover.setTotalTurnoverDebit(turnoverDTO.getTotalTurnoverDebit() != null ?
                turnoverDTO.getTotalTurnoverDebit() : BigDecimal.ZERO);
        turnover.setNumberOfCreditEntries(turnoverDTO.getNumberOfCreditEntries() != null ?
                turnoverDTO.getNumberOfCreditEntries() : 0);

        // Calculate derived fields
        turnover.calculateMonthlyCreditAverage();
        turnover.calculateUtilizationPercentage(header.getSanctionLimit());
        turnover.setHeader(header);

        // Save turnover
        OdTurnover savedTurnover = turnoverRepository.save(turnover);
        log.info("OD turnover record created successfully with ID: {}", savedTurnover.getId());

        return mapToDTO(savedTurnover);
    }

    @Override
    public OdTurnoverDTO updateTurnover(Long id, OdTurnoverDTO turnoverDTO) {
        log.info("Updating OD turnover record with ID: {}", id);

        // Find existing turnover
        OdTurnover existingTurnover = turnoverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turnover record not found with ID: " + id));

        // Validate turnover data
        validateTurnover(turnoverDTO);

        // Check for duplicate month if month is being changed
        if (turnoverDTO.getMonth() != null && !turnoverDTO.getMonth().equals(existingTurnover.getMonth())) {
            boolean monthExists = existingTurnover.getHeader().getTurnoverRecords().stream()
                    .filter(t -> !t.getId().equals(id))
                    .anyMatch(t -> t.getMonth() != null && t.getMonth().equals(turnoverDTO.getMonth()));

            if (monthExists) {
                throw new DuplicateResourceException("Turnover record for month " + turnoverDTO.getMonth() + " already exists");
            }
        }

        // Update fields
        if (turnoverDTO.getMonth() != null) existingTurnover.setMonth(turnoverDTO.getMonth());
        if (turnoverDTO.getTotalTurnoverCredit() != null) existingTurnover.setTotalTurnoverCredit(turnoverDTO.getTotalTurnoverCredit());
        if (turnoverDTO.getTotalTurnoverDebit() != null) existingTurnover.setTotalTurnoverDebit(turnoverDTO.getTotalTurnoverDebit());
        if (turnoverDTO.getNumberOfCreditEntries() != null) existingTurnover.setNumberOfCreditEntries(turnoverDTO.getNumberOfCreditEntries());

        // Recalculate derived fields
        existingTurnover.calculateMonthlyCreditAverage();
        existingTurnover.calculateUtilizationPercentage(existingTurnover.getHeader().getSanctionLimit());

        // Save updated turnover
        OdTurnover updatedTurnover = turnoverRepository.save(existingTurnover);
        log.info("OD turnover record updated successfully with ID: {}", updatedTurnover.getId());

        return mapToDTO(updatedTurnover);
    }

    @Override
    public OdTurnoverDTO getTurnoverById(Long id) {
        log.info("Fetching OD turnover record with ID: {}", id);

        OdTurnover turnover = turnoverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turnover record not found with ID: " + id));

        return mapToDTO(turnover);
    }

    @Override
    public Page<OdTurnoverDTO> getAllTurnovers(Pageable pageable) {
        log.info("Fetching all OD turnover records with pagination");

        return turnoverRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Override
    public void deleteTurnover(Long id) {
        log.info("Deleting OD turnover record with ID: {}", id);

        OdTurnover turnover = turnoverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turnover record not found with ID: " + id));

        turnoverRepository.delete(turnover);
        log.info("OD turnover record deleted successfully with ID: {}", id);
    }

    @Override
    public List<OdTurnoverDTO> getTurnoversByHeaderId(Long headerId) {
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
    public List<OdTurnoverDTO> getTurnoversByMonth(YearMonth month) {
        log.info("Fetching turnover records for month: {}", month);

        return turnoverRepository.findByMonth(month)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OdTurnoverDTO> getTurnoversByMonthRange(YearMonth startMonth, YearMonth endMonth) {
        log.info("Fetching turnover records between {} and {}", startMonth, endMonth);

        return turnoverRepository.findByMonthBetween(startMonth, endMonth)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OdTurnoverDTO getTurnoverByHeaderIdAndMonth(Long headerId, YearMonth month) {
        log.info("Fetching turnover record for header ID: {} and month: {}", headerId, month);

        OdTurnover turnover = turnoverRepository.findByHeaderIdAndMonth(headerId, month)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Turnover record not found for header ID: " + headerId + " and month: " + month));

        return mapToDTO(turnover);
    }

    @Override
    public Map<YearMonth, BigDecimal> getMonthlyCreditTrend(Long headerId) {
        log.info("Generating monthly credit trend for header ID: {}", headerId);

        List<OdTurnover> turnovers = turnoverRepository.findByHeaderId(headerId);

        return turnovers.stream()
                .filter(t -> t.getMonth() != null && t.getTotalTurnoverCredit() != null)
                .collect(Collectors.toMap(
                        OdTurnover::getMonth,
                        OdTurnover::getTotalTurnoverCredit,
                        (v1, v2) -> v1.add(v2),
                        TreeMap::new
                ));
    }

    @Override
    public Map<YearMonth, BigDecimal> getMonthlyDebitTrend(Long headerId) {
        log.info("Generating monthly debit trend for header ID: {}", headerId);

        List<OdTurnover> turnovers = turnoverRepository.findByHeaderId(headerId);

        return turnovers.stream()
                .filter(t -> t.getMonth() != null && t.getTotalTurnoverDebit() != null)
                .collect(Collectors.toMap(
                        OdTurnover::getMonth,
                        OdTurnover::getTotalTurnoverDebit,
                        (v1, v2) -> v1.add(v2),
                        TreeMap::new
                ));
    }

    @Override
    public Map<YearMonth, BigDecimal> getMonthlyUtilizationTrend(Long headerId) {
        log.info("Generating monthly utilization trend for header ID: {}", headerId);

        List<OdTurnover> turnovers = turnoverRepository.findByHeaderId(headerId);

        return turnovers.stream()
                .filter(t -> t.getMonth() != null && t.getUtilizationPercentage() != null)
                .collect(Collectors.toMap(
                        OdTurnover::getMonth,
                        OdTurnover::getUtilizationPercentage,
                        (v1, v2) -> v1,
                        TreeMap::new
                ));
    }

    @Override
    public List<OdTurnoverDTO> getHighUtilizationMonths(Long headerId, BigDecimal threshold) {
        log.info("Fetching high utilization months for header ID: {} with threshold: {}", headerId, threshold);

        return turnoverRepository.findHighUtilizationMonths(headerId, threshold)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getTurnoverSummary(Long headerId) {
        log.info("Generating turnover summary for header ID: {}", headerId);

        BigDecimal totalCredit = turnoverRepository.sumTotalCreditByHeaderId(headerId);
        BigDecimal totalDebit = turnoverRepository.sumTotalDebitByHeaderId(headerId);
        BigDecimal avgUtilization = turnoverRepository.averageUtilizationByHeaderId(headerId);
        Long recordCount = turnoverRepository.countByHeaderId(headerId);

        Optional<OdTurnover> latest = turnoverRepository.findLatestByHeaderId(headerId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("headerId", headerId);
        summary.put("totalCredit", totalCredit);
        summary.put("totalDebit", totalDebit);
        summary.put("netTurnover", totalCredit.subtract(totalDebit));
        summary.put("averageUtilization", avgUtilization);
        summary.put("recordCount", recordCount);
        summary.put("latestMonth", latest.map(OdTurnover::getMonth).orElse(null));
        summary.put("latestUtilization", latest.map(OdTurnover::getUtilizationPercentage).orElse(null));

        return summary;
    }

    // Helper methods
    private OdTurnoverDTO mapToDTO(OdTurnover turnover) {
        return OdTurnoverDTO.builder()
                .id(turnover.getId())
                .month(turnover.getMonth())
                .totalTurnoverCredit(turnover.getTotalTurnoverCredit())
                .totalTurnoverDebit(turnover.getTotalTurnoverDebit())
                .numberOfCreditEntries(turnover.getNumberOfCreditEntries())
                .monthlyCreditAverage(turnover.getMonthlyCreditAverage())
                .utilizationPercentage(turnover.getUtilizationPercentage())
                .headerId(turnover.getHeader() != null ? turnover.getHeader().getId() : null)
                .build();
    }

    private void validateTurnover(OdTurnoverDTO dto) {
        if (dto.getTotalTurnoverCredit() != null && dto.getTotalTurnoverCredit().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Total turnover credit cannot be negative");
        }
        if (dto.getTotalTurnoverDebit() != null && dto.getTotalTurnoverDebit().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Total turnover debit cannot be negative");
        }
        if (dto.getNumberOfCreditEntries() != null && dto.getNumberOfCreditEntries() < 0) {
            throw new BadRequestException("Number of credit entries cannot be negative");
        }
    }
}