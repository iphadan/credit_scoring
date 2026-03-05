package com.cbo.credit_scoring.services.impl;

import com.cbo.credit_scoring.dtos.OdSwingDTO;
import com.cbo.credit_scoring.models.OdSwing;
import com.cbo.credit_scoring.models.OdSwingHeader;
import com.cbo.credit_scoring.repositories.OdSwingRepository;
import com.cbo.credit_scoring.repositories.OdSwingHeaderRepository;
import com.cbo.credit_scoring.services.OdSwingService;
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
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OdSwingServiceImpl implements OdSwingService {

    private final OdSwingRepository swingRepository;
    private final OdSwingHeaderRepository headerRepository;

    @Override
    public OdSwingDTO createSwing(OdSwingDTO swingDTO) {
        log.info("Creating new OD swing record");

        if (swingDTO.getHeaderId() == null) {
            throw new BadRequestException("Header ID is required");
        }

        // Find header
        OdSwingHeader header = headerRepository.findById(swingDTO.getHeaderId())
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + swingDTO.getHeaderId()));

        // Validate swing data
        validateSwing(swingDTO);

        // Validate date logic
        if (swingDTO.getDateHigh() != null && swingDTO.getDateLow() != null) {
            if (swingDTO.getDateHigh().isBefore(swingDTO.getDateLow())) {
                throw new BadRequestException("Date high cannot be before date low");
            }
        }

        // Check for duplicate month
        if (swingDTO.getMonth() != null) {
            boolean monthExists = header.getSwingRecords().stream()
                    .anyMatch(s -> s.getMonth() != null && s.getMonth().equals(swingDTO.getMonth()));

            if (monthExists) {
                throw new DuplicateResourceException("Swing record for month " + swingDTO.getMonth() + " already exists");
            }
        }

        // Create new swing record
        OdSwing swing = new OdSwing();
        swing.setMonth(swingDTO.getMonth());
        swing.setHighestUtilization(swingDTO.getHighestUtilization());
        swing.setDateHigh(swingDTO.getDateHigh());
        swing.setLowestUtilization(swingDTO.getLowestUtilization());
        swing.setDateLow(swingDTO.getDateLow());
        swing.setUtilizationPercentage(swingDTO.getUtilizationPercentage());
        swing.setHeader(header);

        // Save swing
        OdSwing savedSwing = swingRepository.save(swing);
        log.info("OD swing record created successfully with ID: {}", savedSwing.getId());

        return mapToDTO(savedSwing);
    }

    @Override
    public OdSwingDTO updateSwing(Long id, OdSwingDTO swingDTO) {
        log.info("Updating OD swing record with ID: {}", id);

        // Find existing swing
        OdSwing existingSwing = swingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Swing record not found with ID: " + id));

        // Validate swing data
        validateSwing(swingDTO);

        // Validate date logic
        if (swingDTO.getDateHigh() != null && swingDTO.getDateLow() != null) {
            if (swingDTO.getDateHigh().isBefore(swingDTO.getDateLow())) {
                throw new BadRequestException("Date high cannot be before date low");
            }
        }

        // Check for duplicate month if month is being changed
        if (swingDTO.getMonth() != null && !swingDTO.getMonth().equals(existingSwing.getMonth())) {
            boolean monthExists = existingSwing.getHeader().getSwingRecords().stream()
                    .filter(s -> !s.getId().equals(id))
                    .anyMatch(s -> s.getMonth() != null && s.getMonth().equals(swingDTO.getMonth()));

            if (monthExists) {
                throw new DuplicateResourceException("Swing record for month " + swingDTO.getMonth() + " already exists");
            }
        }

        // Update fields
        if (swingDTO.getMonth() != null) existingSwing.setMonth(swingDTO.getMonth());
        if (swingDTO.getHighestUtilization() != null) existingSwing.setHighestUtilization(swingDTO.getHighestUtilization());
        if (swingDTO.getDateHigh() != null) existingSwing.setDateHigh(swingDTO.getDateHigh());
        if (swingDTO.getLowestUtilization() != null) existingSwing.setLowestUtilization(swingDTO.getLowestUtilization());
        if (swingDTO.getDateLow() != null) existingSwing.setDateLow(swingDTO.getDateLow());
        if (swingDTO.getUtilizationPercentage() != null) existingSwing.setUtilizationPercentage(swingDTO.getUtilizationPercentage());

        // Save updated swing
        OdSwing updatedSwing = swingRepository.save(existingSwing);
        log.info("OD swing record updated successfully with ID: {}", updatedSwing.getId());

        return mapToDTO(updatedSwing);
    }

    @Override
    public OdSwingDTO getSwingById(Long id) {
        log.info("Fetching OD swing record with ID: {}", id);

        OdSwing swing = swingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Swing record not found with ID: " + id));

        return mapToDTO(swing);
    }

    @Override
    public Page<OdSwingDTO> getAllSwings(Pageable pageable) {
        log.info("Fetching all OD swing records with pagination");

        return swingRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Override
    public void deleteSwing(Long id) {
        log.info("Deleting OD swing record with ID: {}", id);

        OdSwing swing = swingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Swing record not found with ID: " + id));

        swingRepository.delete(swing);
        log.info("OD swing record deleted successfully with ID: {}", id);
    }

    @Override
    public List<OdSwingDTO> getSwingsByHeaderId(Long headerId) {
        log.info("Fetching swing records for header ID: {}", headerId);

        if (!headerRepository.existsById(headerId)) {
            throw new ResourceNotFoundException("Header not found with ID: " + headerId);
        }

        return swingRepository.findByHeaderId(headerId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OdSwingDTO> getSwingsByMonth(YearMonth month) {
        log.info("Fetching swing records for month: {}", month);

        return swingRepository.findByMonth(month)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OdSwingDTO> getSwingsByMonthRange(YearMonth startMonth, YearMonth endMonth) {
        log.info("Fetching swing records between {} and {}", startMonth, endMonth);

        return swingRepository.findByMonthBetween(startMonth, endMonth)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OdSwingDTO getSwingByHeaderIdAndMonth(Long headerId, YearMonth month) {
        log.info("Fetching swing record for header ID: {} and month: {}", headerId, month);

        OdSwing swing = swingRepository.findByHeaderIdAndMonth(headerId, month)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Swing record not found for header ID: " + headerId + " and month: " + month));

        return mapToDTO(swing);
    }

    @Override
    public List<OdSwingDTO> getSwingsByDateHighRange(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching swing records with date high between {} and {}", startDate, endDate);

        return swingRepository.findByDateHighBetween(startDate, endDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OdSwingDTO> getSwingsByDateLowRange(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching swing records with date low between {} and {}", startDate, endDate);

        return swingRepository.findByDateLowBetween(startDate, endDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<YearMonth, BigDecimal> getMonthlyUtilizationTrend(Long headerId) {
        log.info("Generating monthly utilization trend for header ID: {}", headerId);

        List<OdSwing> swings = swingRepository.findByHeaderIdOrderByMonth(headerId);

        return swings.stream()
                .filter(s -> s.getMonth() != null && s.getUtilizationPercentage() != null)
                .collect(Collectors.toMap(
                        OdSwing::getMonth,
                        OdSwing::getUtilizationPercentage,
                        (v1, v2) -> v1,
                        TreeMap::new
                ));
    }

    @Override
    public Map<YearMonth, BigDecimal> getMonthlySwingRangeTrend(Long headerId) {
        log.info("Generating monthly swing range trend for header ID: {}", headerId);

        List<OdSwing> swings = swingRepository.findByHeaderIdOrderByMonth(headerId);

        return swings.stream()
                .filter(s -> s.getMonth() != null)
                .collect(Collectors.toMap(
                        OdSwing::getMonth,
                        OdSwing::calculateSwingRange,
                        (v1, v2) -> v1,
                        TreeMap::new
                ));
    }

    @Override
    public OdSwingDTO getMonthWithHighestUtilization(Long headerId) {
        log.info("Finding month with highest utilization for header ID: {}", headerId);

        Optional<OdSwing> swing = swingRepository.findMonthWithHighestUtilization(headerId);

        return swing.map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("No swing records found for header ID: " + headerId));
    }

    @Override
    public OdSwingDTO getMonthWithLowestUtilization(Long headerId) {
        log.info("Finding month with lowest utilization for header ID: {}", headerId);

        Optional<OdSwing> swing = swingRepository.findMonthWithLowestUtilization(headerId);

        return swing.map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("No swing records found for header ID: " + headerId));
    }

    @Override
    public OdSwingDTO getMonthWithWidestSwing(Long headerId) {
        log.info("Finding month with widest swing for header ID: {}", headerId);

        Optional<OdSwing> swing = swingRepository.findMonthWithWidestSwing(headerId);

        return swing.map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("No swing records found for header ID: " + headerId));
    }

    @Override
    public List<OdSwingDTO> getHighUtilizationMonths(Long headerId, BigDecimal threshold) {
        log.info("Fetching high utilization months for header ID: {} with threshold: {}", headerId, threshold);

        return swingRepository.findByHeaderId(headerId).stream()
                .filter(s -> s.getUtilizationPercentage() != null &&
                        s.getUtilizationPercentage().compareTo(threshold) > 0)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getSwingSummary(Long headerId) {
        log.info("Generating swing summary for header ID: {}", headerId);

        BigDecimal avgUtilization = swingRepository.averageUtilizationByHeaderId(headerId);
        BigDecimal maxUtilization = swingRepository.maxUtilizationByHeaderId(headerId);
        BigDecimal minUtilization = swingRepository.minUtilizationByHeaderId(headerId);
        BigDecimal avgSwingRange = swingRepository.averageSwingRangeByHeaderId(headerId);
        BigDecimal maxSwingRange = swingRepository.maxSwingRangeByHeaderId(headerId);
        Long recordCount = swingRepository.countByHeaderId(headerId);

        Optional<OdSwing> latest = swingRepository.findLatestByHeaderId(headerId);
        Optional<OdSwing> highest = swingRepository.findMonthWithHighestUtilization(headerId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("headerId", headerId);
        summary.put("averageUtilization", avgUtilization);
        summary.put("maxUtilization", maxUtilization);
        summary.put("minUtilization", minUtilization);
        summary.put("averageSwingRange", avgSwingRange);
        summary.put("maxSwingRange", maxSwingRange);
        summary.put("totalMonths", recordCount);
        summary.put("latestMonth", latest.map(OdSwing::getMonth).orElse(null));
        summary.put("latestUtilization", latest.map(OdSwing::getUtilizationPercentage).orElse(null));
        summary.put("highestUtilizationMonth", highest.map(OdSwing::getMonth).orElse(null));
        summary.put("highestUtilizationValue", highest.map(OdSwing::getUtilizationPercentage).orElse(null));

        return summary;
    }

    // Helper methods
    private OdSwingDTO mapToDTO(OdSwing swing) {
        return OdSwingDTO.builder()
                .id(swing.getId())
                .month(swing.getMonth())
                .highestUtilization(swing.getHighestUtilization())
                .dateHigh(swing.getDateHigh())
                .lowestUtilization(swing.getLowestUtilization())
                .dateLow(swing.getDateLow())
                .utilizationPercentage(swing.getUtilizationPercentage())
                .headerId(swing.getHeader() != null ? swing.getHeader().getId() : null)
                .build();
    }

    private void validateSwing(OdSwingDTO dto) {
        if (dto.getHighestUtilization() != null && dto.getHighestUtilization().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Highest utilization cannot be negative");
        }
        if (dto.getLowestUtilization() != null && dto.getLowestUtilization().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Lowest utilization cannot be negative");
        }
        if (dto.getUtilizationPercentage() != null) {
            if (dto.getUtilizationPercentage().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Utilization percentage cannot be negative");
            }
            if (dto.getUtilizationPercentage().compareTo(new BigDecimal("100")) > 0) {
                throw new BadRequestException("Utilization percentage cannot exceed 100");
            }
        }
    }
}