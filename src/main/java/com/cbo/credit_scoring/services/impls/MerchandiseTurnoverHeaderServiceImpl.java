package com.cbo.credit_scoring.services.impls;


import com.cbo.credit_scoring.dtos.MerchandiseTurnoverHeaderDTO;
import com.cbo.credit_scoring.dtos.MerchandiseTurnoverDTO;
import com.cbo.credit_scoring.dtos.MerchandiseTurnoverDTO;
import com.cbo.credit_scoring.models.MerchandiseTurnoverHeader;
import com.cbo.credit_scoring.models.MerchandiseTurnover;
import com.cbo.credit_scoring.repositories.MerchandiseTurnoverHeaderRepository;
import com.cbo.credit_scoring.repositories.MerchandiseTurnoverRepository;
import com.cbo.credit_scoring.services.MerchandiseTurnoverHeaderService;
import com.cbo.credit_scoring.exceptions.ResourceNotFoundException;
import com.cbo.credit_scoring.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MerchandiseTurnoverHeaderServiceImpl implements MerchandiseTurnoverHeaderService {

    private final MerchandiseTurnoverHeaderRepository headerRepository;
    private final MerchandiseTurnoverRepository turnoverRepository;

    @Override
    public MerchandiseTurnoverHeaderDTO createHeader(MerchandiseTurnoverHeaderDTO headerDTO) {
        log.info("Creating new merchandise turnover header for customer: {}", headerDTO.getCustomerName());

        // Validate required fields
        validateHeader(headerDTO);

        // Convert DTO to Entity
        MerchandiseTurnoverHeader header = mapToEntity(headerDTO);

        // Save header
        MerchandiseTurnoverHeader savedHeader = headerRepository.save(header);
        log.info("Header created successfully with ID: {}", savedHeader.getId());

        return mapToDTO(savedHeader);
    }

    @Override
    public MerchandiseTurnoverHeaderDTO updateHeader(Long id, MerchandiseTurnoverHeaderDTO headerDTO) {
        log.info("Updating merchandise turnover header with ID: {}", id);

        // Find existing header
        MerchandiseTurnoverHeader existingHeader = headerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + id));

        // Validate update data
        validateHeader(headerDTO);

        // Update fields
        updateHeaderFields(existingHeader, headerDTO);

        // Save updated header
        MerchandiseTurnoverHeader updatedHeader = headerRepository.save(existingHeader);
        log.info("Header updated successfully with ID: {}", updatedHeader.getId());

        return mapToDTO(updatedHeader);
    }

    @Override
    public MerchandiseTurnoverHeaderDTO getHeaderById(Long id) {
        log.info("Fetching merchandise turnover header with ID: {}", id);

        MerchandiseTurnoverHeader header = headerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + id));

        return mapToDTO(header);
    }

    @Override
    public Page<MerchandiseTurnoverHeaderDTO> getAllHeaders(Pageable pageable) {
        log.info("Fetching all merchandise turnover headers with pagination");

        return headerRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Override
    public void deleteHeader(Long id) {
        log.info("Deleting merchandise turnover header with ID: {}", id);

        MerchandiseTurnoverHeader header = headerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + id));

        headerRepository.delete(header);
        log.info("Header deleted successfully with ID: {}", id);
    }

    @Override
    public List<MerchandiseTurnoverHeaderDTO> searchByCustomerName(String customerName) {
        log.info("Searching headers by customer name: {}", customerName);

        return headerRepository.findByCustomerNameContainingIgnoreCase(customerName)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MerchandiseTurnoverHeaderDTO> searchByAccountNumber(String accountNumber) {
        log.info("Searching headers by account number: {}", accountNumber);

        return headerRepository.findByAccountNumber(accountNumber)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MerchandiseTurnoverHeaderDTO> searchByDateApprovedBetween(LocalDate startDate, LocalDate endDate) {
        log.info("Searching headers by date approved between: {} and {}", startDate, endDate);

        return headerRepository.findByDateApprovedBetween(startDate, endDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MerchandiseTurnoverHeaderDTO> searchByReportingDate(LocalDate reportingDate) {
        log.info("Searching headers by reporting date: {}", reportingDate);

        return headerRepository.findByReportingDate(reportingDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MerchandiseTurnoverHeaderDTO addTurnoverRecord(Long headerId, MerchandiseTurnoverDTO turnoverDTO) {
        log.info("Adding turnover record to header ID: {}", headerId);

        MerchandiseTurnoverHeader header = headerRepository.findById(headerId)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + headerId));

        // Validate turnover data
        validateTurnover(turnoverDTO);

        // Create new turnover record
        MerchandiseTurnover turnover = new MerchandiseTurnover();
        turnover.setMonth(turnoverDTO.getMonth());
        turnover.setDebitDisbursements(turnoverDTO.getDebitDisbursements());
        turnover.setCreditPrincipalRepayments(turnoverDTO.getCreditPrincipalRepayments());

        // Add to header using helper method
        header.addTurnoverRecord(turnover);

        // Save header (cascade will save turnover)
        MerchandiseTurnoverHeader updatedHeader = headerRepository.save(header);
        log.info("Turnover record added successfully to header ID: {}", headerId);

        return mapToDTO(updatedHeader);
    }

    @Override
    @Transactional
    public MerchandiseTurnoverHeaderDTO removeTurnoverRecord(Long headerId, Long turnoverId) {
        log.info("Removing turnover record ID: {} from header ID: {}", turnoverId, headerId);

        MerchandiseTurnoverHeader header = headerRepository.findById(headerId)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + headerId));

        MerchandiseTurnover turnover = turnoverRepository.findById(turnoverId)
                .orElseThrow(() -> new ResourceNotFoundException("Turnover record not found with ID: " + turnoverId));

        // Verify turnover belongs to header
        if (!turnover.getHeader().getId().equals(headerId)) {
            throw new BadRequestException("Turnover record does not belong to this header");
        }

        // Remove using helper method
        header.removeTurnoverRecord(turnover);

        // Save header (cascade will remove turnover)
        MerchandiseTurnoverHeader updatedHeader = headerRepository.save(header);
        log.info("Turnover record removed successfully from header ID: {}", headerId);

        return mapToDTO(updatedHeader);
    }

    @Override
    @Transactional
    public MerchandiseTurnoverHeaderDTO updateTurnoverRecord(Long headerId, Long turnoverId, MerchandiseTurnoverDTO turnoverDTO) {
        log.info("Updating turnover record ID: {} in header ID: {}", turnoverId, headerId);

        MerchandiseTurnoverHeader header = headerRepository.findById(headerId)
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + headerId));

        MerchandiseTurnover turnover = turnoverRepository.findById(turnoverId)
                .orElseThrow(() -> new ResourceNotFoundException("Turnover record not found with ID: " + turnoverId));

        // Verify turnover belongs to header
        if (!turnover.getHeader().getId().equals(headerId)) {
            throw new BadRequestException("Turnover record does not belong to this header");
        }

        // Validate turnover data
        validateTurnover(turnoverDTO);

        // Update turnover fields
        turnover.setMonth(turnoverDTO.getMonth());
        turnover.setDebitDisbursements(turnoverDTO.getDebitDisbursements());
        turnover.setCreditPrincipalRepayments(turnoverDTO.getCreditPrincipalRepayments());

        // Save turnover
        turnoverRepository.save(turnover);
        log.info("Turnover record updated successfully");

        return mapToDTO(header);
    }

    // Helper methods for mapping and validation
    private MerchandiseTurnoverHeader mapToEntity(MerchandiseTurnoverHeaderDTO dto) {
        MerchandiseTurnoverHeader header = new MerchandiseTurnoverHeader();
        header.setCustomerName(dto.getCustomerName());
        header.setTypeOfFacility(dto.getTypeOfFacility());
        header.setIndustryType(dto.getIndustryType());
        header.setAccountNumber(dto.getAccountNumber());
        header.setApprovedAmount(dto.getApprovedAmount());
        header.setDateApproved(dto.getDateApproved());
        header.setReportingDate(dto.getReportingDate());
        return header;
    }

    private MerchandiseTurnoverHeaderDTO mapToDTO(MerchandiseTurnoverHeader header) {
        MerchandiseTurnoverHeaderDTO dto = new MerchandiseTurnoverHeaderDTO();
        dto.setId(header.getId());
        dto.setCustomerName(header.getCustomerName());
        dto.setTypeOfFacility(header.getTypeOfFacility());
        dto.setIndustryType(header.getIndustryType());
        dto.setAccountNumber(header.getAccountNumber());
        dto.setApprovedAmount(header.getApprovedAmount());
        dto.setDateApproved(header.getDateApproved());
        dto.setReportingDate(header.getReportingDate());

        // Map turnover records if they exist
        if (header.getTurnoverRecords() != null && !header.getTurnoverRecords().isEmpty()) {
            List<MerchandiseTurnoverHeaderDTO.MerchandiseTurnoverDTO> turnoverDTOs =
                    header.getTurnoverRecords().stream()
                            .map(this::mapTurnoverToDTO)
                            .collect(Collectors.toList());
            dto.setTurnoverRecords(turnoverDTOs);
        }

        return dto;
    }

    private MerchandiseTurnoverHeaderDTO.MerchandiseTurnoverDTO mapTurnoverToDTO(MerchandiseTurnover turnover) {
        MerchandiseTurnoverHeaderDTO.MerchandiseTurnoverDTO dto =
                new MerchandiseTurnoverHeaderDTO.MerchandiseTurnoverDTO();
        dto.setId(turnover.getId());
        dto.setMonth(turnover.getMonth());
        dto.setDebitDisbursements(turnover.getDebitDisbursements());
        dto.setCreditPrincipalRepayments(turnover.getCreditPrincipalRepayments());
        return dto;
    }

    private void updateHeaderFields(MerchandiseTurnoverHeader existing, MerchandiseTurnoverHeaderDTO dto) {
        existing.setCustomerName(dto.getCustomerName());
        existing.setTypeOfFacility(dto.getTypeOfFacility());
        existing.setIndustryType(dto.getIndustryType());
        existing.setAccountNumber(dto.getAccountNumber());
        existing.setApprovedAmount(dto.getApprovedAmount());
        existing.setDateApproved(dto.getDateApproved());
        existing.setReportingDate(dto.getReportingDate());
    }

    private void validateHeader(MerchandiseTurnoverHeaderDTO dto) {
        if (dto.getCustomerName() == null || dto.getCustomerName().trim().isEmpty()) {
            throw new BadRequestException("Customer name is required");
        }
        if (dto.getAccountNumber() == null || dto.getAccountNumber().trim().isEmpty()) {
            throw new BadRequestException("Account number is required");
        }
        if (dto.getApprovedAmount() == null || dto.getApprovedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Approved amount must be greater than zero");
        }
    }

    private void validateTurnover(MerchandiseTurnoverDTO dto) {
        if (dto.getMonth() == null) {
            throw new BadRequestException("Month is required");
        }
        if (dto.getDebitDisbursements() == null || dto.getDebitDisbursements().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Debit disbursements cannot be negative");
        }
        if (dto.getCreditPrincipalRepayments() == null || dto.getCreditPrincipalRepayments().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Credit principal repayments cannot be negative");
        }
    }
}
