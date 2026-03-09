package com.cbo.credit_scoring.services.impls;

import com.cbo.credit_scoring.dtos.CollateralRequestDTO;
import com.cbo.credit_scoring.dtos.CollateralResponseDTO;
import com.cbo.credit_scoring.dtos.CollateralSummaryDTO;
import com.cbo.credit_scoring.models.Collateral;
import com.cbo.credit_scoring.models.enums.CollateralType;
import com.cbo.credit_scoring.models.enums.ValuationMethod;
import com.cbo.credit_scoring.repositories.CollateralRepository;
import com.cbo.credit_scoring.services.CollateralService;
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
import java.time.LocalDate;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CollateralServiceImpl implements CollateralService {

    private final CollateralRepository repository;

    // Constants for discount rate calculations (based on Excel)
    private static final BigDecimal PERCENT_100 = new BigDecimal("100");
    private static final BigDecimal PERCENT_90 = new BigDecimal("90");
    private static final BigDecimal PERCENT_80 = new BigDecimal("80");
    private static final BigDecimal PERCENT_70 = new BigDecimal("70");
    private static final BigDecimal PERCENT_60 = new BigDecimal("60");
    private static final BigDecimal PERCENT_50 = new BigDecimal("50");
    private static final BigDecimal PERCENT_40 = new BigDecimal("40");
    private static final BigDecimal PERCENT_25 = new BigDecimal("25");
    private static final BigDecimal PERCENT_0 = BigDecimal.ZERO;

    @Override
    public CollateralResponseDTO createCollateral(CollateralRequestDTO requestDTO) {
        log.info("Creating collateral for case: {}", requestDTO.getCaseId());

        // Validate request
        validateRequest(requestDTO);

        // Check if records already exist for this case
        List<Collateral> existing = repository.findByCaseIdOrderBySrNoAsc(requestDTO.getCaseId());
        if (!existing.isEmpty()) {
            throw new DuplicateResourceException("Collateral already exists for case: " + requestDTO.getCaseId());
        }

        // Create and save all records
        List<Collateral> savedRecords = new ArrayList<>();
        int srNo = 1;

        for (CollateralRequestDTO.CollateralRecord recordDTO : requestDTO.getCollaterals()) {
            // Validate each record
            validateCollateralRecord(recordDTO);

            Collateral entity = mapToEntity(requestDTO, recordDTO, srNo++);
            savedRecords.add(repository.save(entity));
        }

        log.info("Successfully created {} collateral records for case: {}",
                savedRecords.size(), requestDTO.getCaseId());

        return buildResponse(savedRecords, requestDTO.getReportingDate());
    }

    @Override
    public CollateralResponseDTO updateCollateral(String caseId, CollateralRequestDTO requestDTO) {
        log.info("Updating collateral for case: {}", caseId);

        // Validate request
        validateRequest(requestDTO);

        // Verify caseId matches
        if (!caseId.equals(requestDTO.getCaseId())) {
            throw new BadRequestException("Case ID in path does not match case ID in request body");
        }

        // Delete existing records
        deleteByCaseId(caseId);

        // Create new records
        return createCollateral(requestDTO);
    }

    @Override
    public CollateralResponseDTO getCollateralByCaseId(String caseId) {
        log.info("Fetching collateral for case: {}", caseId);

        List<Collateral> records = repository.findByCaseIdOrderBySrNoAsc(caseId);

        if (records.isEmpty()) {
            throw new ResourceNotFoundException("No collateral found for case: " + caseId);
        }

        return buildResponse(records, records.get(0).getCreatedAt());
    }

    @Override
    public CollateralResponseDTO.CollateralRecord getCollateralRecordById(Long id) {
        log.info("Fetching collateral record with ID: {}", id);

        Collateral record = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collateral record not found with ID: " + id));

        return mapToRecordDTO(record);
    }

    @Override
    public Page<CollateralResponseDTO.CollateralRecord> getAllCollateralRecords(Pageable pageable) {
        log.info("Fetching all collateral records with pagination");

        return repository.findAll(pageable)
                .map(this::mapToRecordDTO);
    }

    @Override
    public void deleteByCaseId(String caseId) {
        log.info("Deleting all collateral for case: {}", caseId);

        List<Collateral> records = repository.findByCaseIdOrderBySrNoAsc(caseId);
        if (records.isEmpty()) {
            throw new ResourceNotFoundException("No collateral found for case: " + caseId);
        }

        repository.deleteAll(records);
        log.info("Deleted {} collateral records for case: {}", records.size(), caseId);
    }

    @Override
    public void deleteCollateralRecord(Long id) {
        log.info("Deleting collateral record with ID: {}", id);

        Collateral record = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collateral record not found with ID: " + id));

        repository.delete(record);
        log.info("Deleted collateral record with ID: {}", id);
    }

    @Override
    public CollateralSummaryDTO getCollateralSummary(String caseId) {
        log.info("Generating collateral summary for case: {}", caseId);

        List<Collateral> records = repository.findByCaseIdOrderBySrNoAsc(caseId);

        if (records.isEmpty()) {
            throw new ResourceNotFoundException("No collateral found for case: " + caseId);
        }

        return buildSummary(records);
    }

    @Override
    public CollateralSummaryDTO calculateTotals(String caseId) {
        return getCollateralSummary(caseId);
    }

    @Override
    public Integer calculateAge(Integer yearOfManufacturing) {
        if (yearOfManufacturing == null) {
            return null;
        }
        int currentYear = Year.now().getValue();
        return currentYear - yearOfManufacturing;
    }

    @Override
    public Boolean calculateMandatoryMethod(CollateralType type, Integer age, ValuationMethod method) {
        // Based on Excel: =IF(OR(B4='[1]Security Related Factor'!$D$10,B4='[1]Security Related Factor'!$D$11),IF(AND(D4>3,F4="Invoice"),0,1))
        // This is simplified - actual implementation would need the security factors table
        if (type == CollateralType.VEHICLE || type == CollateralType.MERCHANDIZE) {
            if (age != null && age > 3 && method == ValuationMethod.INVOICE) {
                return false; // 0
            }
            return true; // 1
        }
        return null; // Not applicable for other types
    }

    @Override
    public BigDecimal calculateDiscountRate(CollateralType type, Integer age, ValuationMethod method) {
        // This implements the complex Excel nested IF logic
        // Default for non-vehicle/non-merchandize
        if (type != CollateralType.VEHICLE && type != CollateralType.MERCHANDIZE) {
            return PERCENT_100;
        }

        if (age == null) {
            return PERCENT_0;
        }

        // Vehicle or Merchandize logic
        if (method == ValuationMethod.INVOICE) {
            if (age <= 1) return PERCENT_90;
            if (age <= 3) return PERCENT_80;
            if (age > 4) return PERCENT_0;
        } else if (method == ValuationMethod.INSURANCE) {
            if (age <= 1) return PERCENT_80;
            if (age <= 3) return PERCENT_70;
            if (age <= 5) return PERCENT_60;
            if (age <= 7) return PERCENT_50;
            if (age <= 10) return PERCENT_40;
            if (age <= 15) return PERCENT_25;
            if (age > 15) return PERCENT_0;
        }

        return PERCENT_0;
    }

    @Override
    public BigDecimal calculateNetValue(BigDecimal value, BigDecimal discountRate) {
        if (value == null || discountRate == null) {
            return BigDecimal.ZERO;
        }
        return value.multiply(discountRate)
                .divide(PERCENT_100, 2, RoundingMode.HALF_UP);
    }

    // ============= Helper Methods =============

    private void validateRequest(CollateralRequestDTO dto) {
        if (dto.getCaseId() == null || dto.getCaseId().trim().isEmpty()) {
            throw new BadRequestException("Case ID is required");
        }
        if (dto.getCollaterals() == null || dto.getCollaterals().isEmpty()) {
            throw new BadRequestException("At least one collateral record is required");
        }
    }

    private void validateCollateralRecord(CollateralRequestDTO.CollateralRecord dto) {
        if (dto.getCollateralType() == null) {
            throw new BadRequestException("Collateral type is required");
        }
        if (dto.getValue() == null || dto.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Value must be positive");
        }
        if (dto.getValuationMethod() == null) {
            throw new BadRequestException("Valuation method is required");
        }
    }

    private Collateral mapToEntity(CollateralRequestDTO requestDTO,
                                   CollateralRequestDTO.CollateralRecord recordDTO,
                                   int srNo) {
        return Collateral.builder()
                .caseId(requestDTO.getCaseId())
                .srNo(srNo)
                .collateralType(recordDTO.getCollateralType())
                .yearOfManufacturing(recordDTO.getYearOfManufacturing())
                .value(recordDTO.getValue())
                .valuationMethod(recordDTO.getValuationMethod())
                .build();
    }

    private CollateralResponseDTO buildResponse(List<Collateral> records, LocalDate reportingDate) {
        List<CollateralResponseDTO.CollateralRecord> recordDTOs = records.stream()
                .map(this::mapToRecordDTO)
                .collect(Collectors.toList());

        BigDecimal totalValue = records.stream()
                .map(Collateral::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalNetValue = recordDTOs.stream()
                .map(CollateralResponseDTO.CollateralRecord::getNetValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Find max percentage collateral
        CollateralResponseDTO.MaxPercentageCollateral maxPercentage = findMaxPercentageCollateral(recordDTOs, totalNetValue);

        return CollateralResponseDTO.builder()
                .caseId(records.get(0).getCaseId())
                .reportingDate(reportingDate)
                .collaterals(recordDTOs)
                .totalValue(totalValue)
                .totalNetValue(totalNetValue)
                .totalRecords(records.size())
                .maxPercentageCollateral(maxPercentage)
                .build();
    }

    private CollateralResponseDTO.CollateralRecord mapToRecordDTO(Collateral entity) {
        Integer age = calculateAge(entity.getYearOfManufacturing());
        BigDecimal discountRate = calculateDiscountRate(
                entity.getCollateralType(), age, entity.getValuationMethod());
        BigDecimal netValue = calculateNetValue(entity.getValue(), discountRate);

        return CollateralResponseDTO.CollateralRecord.builder()
                .id(entity.getId())
                .srNo(entity.getSrNo())
                .collateralType(entity.getCollateralType())
                .yearOfManufacturing(entity.getYearOfManufacturing())
                .age(age)
                .value(entity.getValue())
                .valuationMethod(entity.getValuationMethod())
                .mandatoryMethod(calculateMandatoryMethod(
                        entity.getCollateralType(), age, entity.getValuationMethod()))
                .discountRate(discountRate)
                .netValue(netValue)
                .build();
    }

    private CollateralResponseDTO.MaxPercentageCollateral findMaxPercentageCollateral(
            List<CollateralResponseDTO.CollateralRecord> records, BigDecimal totalNetValue) {

        if (records.isEmpty() || totalNetValue.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return records.stream()
                .max(Comparator.comparing(CollateralResponseDTO.CollateralRecord::getNetValue))
                .map(record -> {
                    BigDecimal percentage = record.getNetValue()
                            .multiply(PERCENT_100)
                            .divide(totalNetValue, 2, RoundingMode.HALF_UP);
                    return CollateralResponseDTO.MaxPercentageCollateral.builder()
                            .collateralType(record.getCollateralType().getDisplayName())
                            .netValue(record.getNetValue())
                            .percentage(percentage)
                            .build();
                })
                .orElse(null);
    }

    private CollateralSummaryDTO buildSummary(List<Collateral> records) {
        String caseId = records.get(0).getCaseId();

        // Calculate records with their net values
        List<CollateralResponseDTO.CollateralRecord> recordDTOs = records.stream()
                .map(this::mapToRecordDTO)
                .collect(Collectors.toList());

        BigDecimal totalValue = repository.sumValueByCaseId(caseId);
        BigDecimal totalNetValue = recordDTOs.stream()
                .map(CollateralResponseDTO.CollateralRecord::getNetValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Group by collateral type
        Map<CollateralType, CollateralSummaryDTO.TypeSummary> byType = new HashMap<>();
        List<Object[]> typeGroups = repository.groupByCollateralType(caseId);
        for (Object[] group : typeGroups) {
            CollateralType type = (CollateralType) group[0];
            Long count = (Long) group[1];
            BigDecimal value = (BigDecimal) group[2];

            // Calculate net value for this type
            BigDecimal netValue = recordDTOs.stream()
                    .filter(r -> r.getCollateralType() == type)
                    .map(CollateralResponseDTO.CollateralRecord::getNetValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            byType.put(type, CollateralSummaryDTO.TypeSummary.builder()
                    .count(count)
                    .totalValue(value)
                    .totalNetValue(netValue)
                    .build());
        }

        // Group by valuation method
        Map<ValuationMethod, CollateralSummaryDTO.MethodSummary> byMethod = new HashMap<>();
        List<Object[]> methodGroups = repository.groupByValuationMethod(caseId);
        for (Object[] group : methodGroups) {
            ValuationMethod method = (ValuationMethod) group[0];
            Long count = (Long) group[1];
            BigDecimal value = (BigDecimal) group[2];

            // Calculate net value for this method
            BigDecimal netValue = recordDTOs.stream()
                    .filter(r -> r.getValuationMethod() == method)
                    .map(CollateralResponseDTO.CollateralRecord::getNetValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            byMethod.put(method, CollateralSummaryDTO.MethodSummary.builder()
                    .count(count)
                    .totalValue(value)
                    .totalNetValue(netValue)
                    .build());
        }

        // Find max percentage collateral
        CollateralSummaryDTO.MaxPercentageDetail maxPercentage = null;
        if (!recordDTOs.isEmpty() && totalNetValue.compareTo(BigDecimal.ZERO) > 0) {
            CollateralResponseDTO.CollateralRecord max = recordDTOs.stream()
                    .max(Comparator.comparing(CollateralResponseDTO.CollateralRecord::getNetValue))
                    .orElse(null);

            if (max != null) {
                BigDecimal percentage = max.getNetValue()
                        .multiply(PERCENT_100)
                        .divide(totalNetValue, 2, RoundingMode.HALF_UP);

                maxPercentage = CollateralSummaryDTO.MaxPercentageDetail.builder()
                        .collateralType(max.getCollateralType().getDisplayName())
                        .netValue(max.getNetValue())
                        .percentage(percentage)
                        .build();
            }
        }

        return CollateralSummaryDTO.builder()
                .caseId(caseId)
                .reportingDate(records.get(0).getCreatedAt())
                .totalValue(totalValue)
                .totalNetValue(totalNetValue)
                .totalRecords(records.size())
                .byCollateralType(byType)
                .byValuationMethod(byMethod)
                .maxPercentageCollateral(maxPercentage)
                .build();
    }
}