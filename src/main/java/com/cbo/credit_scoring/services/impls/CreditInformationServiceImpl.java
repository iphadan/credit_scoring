package com.cbo.credit_scoring.services.impls;

import com.cbo.credit_scoring.dtos.CreditInformationRequestDTO;
import com.cbo.credit_scoring.dtos.CreditInformationResponseDTO;
import com.cbo.credit_scoring.dtos.CreditSummaryDTO;
import com.cbo.credit_scoring.models.CreditInformation;
import com.cbo.credit_scoring.models.enums.*;
import com.cbo.credit_scoring.repositories.CreditInformationRepository;
import com.cbo.credit_scoring.services.CreditInformationService;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreditInformationServiceImpl implements CreditInformationService {

    private final CreditInformationRepository repository;
// Add these methods to existing CreditInformationServiceImpl.java




    @Override
    public List<String> getAllCaseIds() {
        log.info("Fetching all unique caseIds from credit information module");

        return repository.findAllCaseIds();
    }
    @Override
    public List<CreditInformationResponseDTO> createCreditInformation(CreditInformationRequestDTO requestDTO) {
        log.info("Creating credit information for case: {}, bank type: {}",
                requestDTO.getCaseId(), requestDTO.getBankType());

        // Validate request
        validateRequest(requestDTO);

        // Check if records already exist for this case and bank type
        List<CreditInformation> existing = repository.findByCaseIdAndBankType(
                requestDTO.getCaseId(), requestDTO.getBankType());

        if (!existing.isEmpty()) {
            throw new DuplicateResourceException(
                    "Credit information already exists for case: " + requestDTO.getCaseId() +
                            " and bank type: " + requestDTO.getBankType());
        }

        // Create and save all records
        List<CreditInformation> savedRecords = new ArrayList<>();
        int srNo = 1;

        for (CreditInformationRequestDTO.ExposureRecord exposureDTO : requestDTO.getExposures()) {
            // Validate each exposure
            validateExposure(exposureDTO, requestDTO.getBankType());

            CreditInformation entity = mapToEntity(requestDTO, exposureDTO, srNo++);
            savedRecords.add(repository.save(entity));
        }

        log.info("Successfully created {} credit records for case: {}, bank type: {}",
                savedRecords.size(), requestDTO.getCaseId(), requestDTO.getBankType());

        // Return as a list with a single response DTO (since it's all same bank type)
        return Collections.singletonList(mapToDTOList(savedRecords, requestDTO.getBankType()));
    }

    @Override
    public List<CreditInformationResponseDTO> updateCreditInformation(String caseId, BankType bankType,
                                                                      CreditInformationRequestDTO requestDTO) {
        log.info("Updating credit information for case: {}, bank type: {}", caseId, bankType);

        // Validate request
        validateRequest(requestDTO);

        // Verify caseId matches
        if (!caseId.equals(requestDTO.getCaseId())) {
            throw new BadRequestException("Case ID in path does not match case ID in request body");
        }

        // Verify bank type matches
        if (bankType != requestDTO.getBankType()) {
            throw new BadRequestException("Bank type in path does not match bank type in request body");
        }

        // Delete existing records
        deleteByCaseIdAndBankType(caseId, bankType);

        // Create new records
        return createCreditInformation(requestDTO);
    }

    @Override
    public List<CreditInformationResponseDTO> getCreditInformationByCaseId(String caseId) {
        log.info("Fetching credit information for case: {}", caseId);

        List<CreditInformation> records = repository.findByCaseIdOrderByBankTypeAscSrNoAsc(caseId);

        if (records.isEmpty()) {
            throw new ResourceNotFoundException("No credit information found for case: " + caseId);
        }

        // Group by bank type
        Map<BankType, List<CreditInformation>> grouped = records.stream()
                .collect(Collectors.groupingBy(CreditInformation::getBankType));

        List<CreditInformationResponseDTO> responses = new ArrayList<>();

        for (Map.Entry<BankType, List<CreditInformation>> entry : grouped.entrySet()) {
            responses.add(mapToDTOList(entry.getValue(), entry.getKey()));
        }

        return responses;
    }

    @Override
    public CreditInformationResponseDTO getCreditInformationByCaseIdAndBankType(String caseId, BankType bankType) {
        log.info("Fetching credit information for case: {}, bank type: {}", caseId, bankType);

        List<CreditInformation> records = repository.findByCaseIdAndBankType(caseId, bankType);

        if (records.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No credit information found for case: " + caseId + " and bank type: " + bankType);
        }

        return mapToDTOList(records, bankType);
    }

    @Override
    public CreditInformationResponseDTO getCreditRecordById(Long id) {
        log.info("Fetching credit record with ID: {}", id);

        CreditInformation record = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credit record not found with ID: " + id));

        return mapToDTOSingle(record);
    }

    @Override
    public Page<CreditInformationResponseDTO> getAllCreditRecords(Pageable pageable) {
        log.info("Fetching all credit records with pagination");

        return repository.findAll(pageable)
                .map(this::mapToDTOSingle);
    }

    @Override
    public void deleteByCaseId(String caseId) {
        log.info("Deleting all credit information for case: {}", caseId);

        List<CreditInformation> records = repository.findByCaseId(caseId);
        if (records.isEmpty()) {
            throw new ResourceNotFoundException("No credit information found for case: " + caseId);
        }

        repository.deleteAll(records);
        log.info("Deleted {} credit records for case: {}", records.size(), caseId);
    }

    @Override
    public void deleteByCaseIdAndBankType(String caseId, BankType bankType) {
        log.info("Deleting credit information for case: {}, bank type: {}", caseId, bankType);

        List<CreditInformation> records = repository.findByCaseIdAndBankType(caseId, bankType);
        if (records.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No credit information found for case: " + caseId + " and bank type: " + bankType);
        }

        repository.deleteAll(records);
        log.info("Deleted {} credit records for case: {}, bank type: {}",
                records.size(), caseId, bankType);
    }

    @Override
    public void deleteCreditRecord(Long id) {
        log.info("Deleting credit record with ID: {}", id);

        CreditInformation record = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credit record not found with ID: " + id));

        repository.delete(record);
        log.info("Deleted credit record with ID: {}", id);
    }

    @Override
    public CreditSummaryDTO getCreditSummary(String caseId) {
        log.info("Generating credit summary for case: {}", caseId);

        List<CreditInformation> records = repository.findByCaseId(caseId);

        if (records.isEmpty()) {
            throw new ResourceNotFoundException("No credit information found for case: " + caseId);
        }

        return buildSummary(records, caseId);
    }

    @Override
    public CreditSummaryDTO calculateTotals(String caseId) {
        return getCreditSummary(caseId); // Same as summary for now
    }

    @Override
    public boolean validateCreditInformation(String caseId) {
        try {
            List<CreditInformation> records = repository.findByCaseId(caseId);
            if (records.isEmpty()) {
                return false;
            }

            // Validate each record
            for (CreditInformation record : records) {
                if (record.getDateGranted() != null && record.getExpiryDate() != null) {
                    if (record.getDateGranted().isAfter(record.getExpiryDate())) {
                        log.warn("Date granted after expiry date for record ID: {}", record.getId());
                        return false;
                    }
                }
                if (record.getAmountGranted() != null && record.getCurrentBalance() != null) {
                    if (record.getCurrentBalance().compareTo(record.getAmountGranted()) > 0) {
                        log.warn("Current balance exceeds amount granted for record ID: {}", record.getId());
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            log.error("Error validating credit information: {}", e.getMessage());
            return false;
        }
    }

    // ============= Helper Methods =============

    private void validateRequest(CreditInformationRequestDTO dto) {
        if (dto.getCaseId() == null || dto.getCaseId().trim().isEmpty()) {
            throw new BadRequestException("Case ID is required");
        }
        if (dto.getBankType() == null) {
            throw new BadRequestException("Bank type is required");
        }
        if (dto.getExposures() == null || dto.getExposures().isEmpty()) {
            throw new BadRequestException("At least one exposure record is required");
        }
    }

    private void validateExposure(CreditInformationRequestDTO.ExposureRecord dto, BankType bankType) {
        System.out.println(bankType);
        if (dto.getExposureType() == null) {
            throw new BadRequestException("Exposure type is required");
        }
        if (dto.getCreditProduct() == null) {
            throw new BadRequestException("Credit product is required");
        }
        if (dto.getAmountGranted() == null || dto.getAmountGranted().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount granted must be positive");
        }
        if (dto.getCurrentBalance() == null || dto.getCurrentBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Current balance cannot be negative");
        }
        if (dto.getStatus() == null) {
            throw new BadRequestException("Status is required");
        }
        if (dto.getDateGranted() != null && dto.getExpiryDate() != null) {
            if (dto.getDateGranted().isAfter(dto.getExpiryDate())) {
                throw new BadRequestException("Date granted cannot be after expiry date");
            }
        }
        if (bankType == BankType.OTHER_BANK &&
                (dto.getLendingBank() == null || dto.getLendingBank().trim().isEmpty())) {
            throw new BadRequestException("Lending bank name is required for other bank exposures");
        }
    }

    private CreditInformation mapToEntity(CreditInformationRequestDTO requestDTO,
                                          CreditInformationRequestDTO.ExposureRecord exposureDTO,
                                          int srNo) {
        CreditInformation entity = new CreditInformation();
        entity.setCaseId(requestDTO.getCaseId());
        entity.setBankType(requestDTO.getBankType());
        entity.setReportingDate(requestDTO.getReportingDate() != null ?
                requestDTO.getReportingDate() : LocalDate.now());
        entity.setSrNo(srNo);
        entity.setExposureType(exposureDTO.getExposureType());
        entity.setExistingExposure(exposureDTO.getExistingExposure());
        entity.setLendingBank(exposureDTO.getLendingBank());
        entity.setCreditProduct(exposureDTO.getCreditProduct());
        entity.setDateGranted(exposureDTO.getDateGranted());
        entity.setExpiryDate(exposureDTO.getExpiryDate());
        entity.setAmountGranted(exposureDTO.getAmountGranted());
        entity.setCurrentBalance(exposureDTO.getCurrentBalance());
        entity.setStatus(exposureDTO.getStatus());
        return entity;
    }

    private CreditInformationResponseDTO mapToDTOList(List<CreditInformation> records, BankType bankType) {
        List<CreditInformationResponseDTO.ExposureRecord> exposureDTOs = records.stream()
                .map(this::mapToExposureDTO)
                .collect(Collectors.toList());

        BigDecimal totalAmountGranted = records.stream()
                .map(CreditInformation::getAmountGranted)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCurrentBalance = records.stream()
                .map(CreditInformation::getCurrentBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Use the first record's caseId and reporting date (they should be the same for all)
        String caseId = records.isEmpty() ? null : records.get(0).getCaseId();
        LocalDate reportingDate = records.isEmpty() ? null : records.get(0).getReportingDate();

        return CreditInformationResponseDTO.builder()
                .caseId(caseId)
                .bankType(bankType)
                .reportingDate(reportingDate)
                .exposures(exposureDTOs)
                .totalAmountGranted(totalAmountGranted)
                .totalCurrentBalance(totalCurrentBalance)
                .totalRecords(records.size())
                .build();
    }

    private CreditInformationResponseDTO mapToDTOSingle(CreditInformation record) {
        List<CreditInformationResponseDTO.ExposureRecord> exposureDTOs = new ArrayList<>();
        exposureDTOs.add(mapToExposureDTO(record));

        return CreditInformationResponseDTO.builder()
                .caseId(record.getCaseId())
                .bankType(record.getBankType())
                .reportingDate(record.getReportingDate())
                .exposures(exposureDTOs)
                .totalAmountGranted(record.getAmountGranted())
                .totalCurrentBalance(record.getCurrentBalance())
                .totalRecords(1)
                .build();
    }

    private CreditInformationResponseDTO.ExposureRecord mapToExposureDTO(CreditInformation entity) {
        return CreditInformationResponseDTO.ExposureRecord.builder()
                .id(entity.getId())
                .srNo(entity.getSrNo())
                .exposureType(entity.getExposureType())
                .existingExposure(entity.getExistingExposure())
                .lendingBank(entity.getLendingBank())
                .creditProduct(entity.getCreditProduct())
                .dateGranted(entity.getDateGranted())
                .expiryDate(entity.getExpiryDate())
                .amountGranted(entity.getAmountGranted())
                .currentBalance(entity.getCurrentBalance())
                .status(entity.getStatus())
                .build();
    }

    private CreditSummaryDTO buildSummary(List<CreditInformation> records, String caseId) {
        // Separate Coop and Other bank records
        List<CreditInformation> coopRecords = records.stream()
                .filter(r -> r.getBankType() == BankType.COOP_BANK)
                .collect(Collectors.toList());

        List<CreditInformation> otherRecords = records.stream()
                .filter(r -> r.getBankType() == BankType.OTHER_BANK)
                .collect(Collectors.toList());

        CreditSummaryDTO.BankSummary coopSummary = buildBankSummary(coopRecords, BankType.COOP_BANK);
        CreditSummaryDTO.BankSummary otherSummary = buildBankSummary(otherRecords, BankType.OTHER_BANK);

        BigDecimal grandTotalGranted = repository.sumTotalAmountGrantedByCaseId(caseId);
        BigDecimal grandTotalBalance = repository.sumTotalCurrentBalanceByCaseId(caseId);

        LocalDate reportingDate = records.isEmpty() ? null : records.get(0).getReportingDate();

        return CreditSummaryDTO.builder()
                .caseId(caseId)
                .reportingDate(reportingDate)
                .coopBankSummary(coopSummary)
                .otherBankSummary(otherSummary)
                .grandTotalAmountGranted(grandTotalGranted)
                .grandTotalCurrentBalance(grandTotalBalance)
                .build();
    }

    private CreditSummaryDTO.BankSummary buildBankSummary(List<CreditInformation> records, BankType bankType) {
        if (records.isEmpty()) {
            return CreditSummaryDTO.BankSummary.builder()
                    .bankType(bankType)
                    .totalAmountGranted(BigDecimal.ZERO)
                    .totalCurrentBalance(BigDecimal.ZERO)
                    .totalRecords(0)
                    .byExposureType(new HashMap<>())
                    .byStatus(new HashMap<>())
                    .build();
        }

        String caseId = records.get(0).getCaseId();

        // Get totals
        BigDecimal totalAmountGranted = repository.sumAmountGrantedByCaseIdAndBankType(caseId, bankType);
        BigDecimal totalCurrentBalance = repository.sumCurrentBalanceByCaseIdAndBankType(caseId, bankType);
        Long totalRecords = repository.countByCaseIdAndBankType(caseId, bankType);

        // Group by exposure type
        Map<ExposureType, CreditSummaryDTO.TypeSummary> byExposureType = new HashMap<>();
        List<Object[]> exposureGroups = repository.groupByExposureType(caseId, bankType);
        for (Object[] group : exposureGroups) {
            ExposureType type = (ExposureType) group[0];
            Long count = (Long) group[1];
            BigDecimal amount = (BigDecimal) group[2];
            BigDecimal balance = (BigDecimal) group[3];

            byExposureType.put(type, CreditSummaryDTO.TypeSummary.builder()
                    .count(count)
                    .totalAmountGranted(amount)
                    .totalCurrentBalance(balance)
                    .build());
        }

        // Group by status
        Map<FacilityStatus, CreditSummaryDTO.StatusSummary> byStatus = new HashMap<>();
        List<Object[]> statusGroups = repository.groupByStatus(caseId, bankType);
        for (Object[] group : statusGroups) {
            FacilityStatus status = (FacilityStatus) group[0];
            Long count = (Long) group[1];
            BigDecimal amount = (BigDecimal) group[2];
            BigDecimal balance = (BigDecimal) group[3];

            byStatus.put(status, CreditSummaryDTO.StatusSummary.builder()
                    .count(count)
                    .totalAmountGranted(amount)
                    .totalCurrentBalance(balance)
                    .build());
        }

        return CreditSummaryDTO.BankSummary.builder()
                .bankType(bankType)
                .totalAmountGranted(totalAmountGranted)
                .totalCurrentBalance(totalCurrentBalance)
                .totalRecords(totalRecords.intValue())
                .byExposureType(byExposureType)
                .byStatus(byStatus)
                .build();
    }
}