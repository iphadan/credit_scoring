package com.cbo.credit_scoring.services.impls;


import com.cbo.credit_scoring.dtos.MerchandiseTurnoverDTO;
import com.cbo.credit_scoring.dtos.MerchandiseTurnoverDTO;
import com.cbo.credit_scoring.models.MerchandiseTurnover;
import com.cbo.credit_scoring.models.MerchandiseTurnoverHeader;
import com.cbo.credit_scoring.repositories.MerchandiseTurnoverRepository;
import com.cbo.credit_scoring.repositories.MerchandiseTurnoverHeaderRepository;
import com.cbo.credit_scoring.services.MerchandiseTurnoverService;
import com.cbo.credit_scoring.exceptions.ResourceNotFoundException;
import com.cbo.credit_scoring.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MerchandiseTurnoverServiceImpl implements MerchandiseTurnoverService {

    private final MerchandiseTurnoverRepository turnoverRepository;
    private final MerchandiseTurnoverHeaderRepository headerRepository;

    @Override
    public MerchandiseTurnoverDTO createTurnover(MerchandiseTurnoverDTO turnoverDTO) {
        log.info("Creating new turnover record");

        // Validate turnover data
        validateTurnover(turnoverDTO);

        // Find header
        MerchandiseTurnoverHeader header = headerRepository.findById(turnoverDTO.getHeaderId())
                .orElseThrow(() -> new ResourceNotFoundException("Header not found with ID: " + turnoverDTO.getHeaderId()));

        // Check for duplicate month in same header
        boolean monthExists = header.getTurnoverRecords().stream()
                .anyMatch(t -> t.getMonth().equals(turnoverDTO.getMonth()));

        if (monthExists) {
            throw new BadRequestException("Turnover record for month " + turnoverDTO.getMonth() + " already exists in this header");
        }

        // Create new turnover record
        MerchandiseTurnover turnover = new MerchandiseTurnover();
        turnover.setMonth(turnoverDTO.getMonth());
        turnover.setDebitDisbursements(turnoverDTO.getDebitDisbursements());
        turnover.setCreditPrincipalRepayments(turnoverDTO.getCreditPrincipalRepayments());
        turnover.setHeader(header);

        // Save turnover
        MerchandiseTurnover savedTurnover = turnoverRepository.save(turnover);
        log.info("Turnover record created successfully with ID: {}", savedTurnover.getId());

        return mapToDTO(savedTurnover);
    }

    @Override
    public MerchandiseTurnoverDTO updateTurnover(Long id, MerchandiseTurnoverDTO turnoverDTO) {
        log.info("Updating turnover record with ID: {}", id);

        // Find existing turnover
        MerchandiseTurnover existingTurnover = turnoverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turnover record not found with ID: " + id));

        // Validate turnover data
        validateTurnover(turnoverDTO);

        // Check for duplicate month if month is being changed
        if (!existingTurnover.getMonth().equals(turnoverDTO.getMonth())) {
            boolean monthExists = existingTurnover.getHeader().getTurnoverRecords().stream()
                    .filter(t -> !t.getId().equals(id))
                    .anyMatch(t -> t.getMonth().equals(turnoverDTO.getMonth()));

            if (monthExists) {
                throw new BadRequestException("Turnover record for month " + turnoverDTO.getMonth() + " already exists in this header");
            }
        }

        // Update fields
        existingTurnover.setMonth(turnoverDTO.getMonth());
        existingTurnover.setDebitDisbursements(turnoverDTO.getDebitDisbursements());
        existingTurnover.setCreditPrincipalRepayments(turnoverDTO.getCreditPrincipalRepayments());

        // Save updated turnover
        MerchandiseTurnover updatedTurnover = turnoverRepository.save(existingTurnover);
        log.info("Turnover record updated successfully with ID: {}", updatedTurnover.getId());

        return mapToDTO(updatedTurnover);
    }

    @Override
    public MerchandiseTurnoverDTO getTurnoverById(Long id) {
        log.info("Fetching turnover record with ID: {}", id);

        MerchandiseTurnover turnover = turnoverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turnover record not found with ID: " + id));

        return mapToDTO(turnover);
    }

    @Override
    public Page<MerchandiseTurnoverDTO> getAllTurnovers(Pageable pageable) {
        log.info("Fetching all turnover records with pagination");

        return turnoverRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Override
    public void deleteTurnover(Long id) {
        log.info("Deleting turnover record with ID: {}", id);

        MerchandiseTurnover turnover = turnoverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turnover record not found with ID: " + id));

        turnoverRepository.delete(turnover);
        log.info("Turnover record deleted successfully with ID: {}", id);
    }

    @Override
    public List<MerchandiseTurnoverDTO> getTurnoversByHeaderId(Long headerId) {
        log.info("Fetching turnover records for header ID: {}", headerId);

        return turnoverRepository.findByHeaderId(headerId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MerchandiseTurnoverDTO> getTurnoversByMonth(YearMonth month) {
        log.info("Fetching turnover records for month: {}", month);

        return turnoverRepository.findByMonth(month)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MerchandiseTurnoverDTO> getTurnoversByDateRange(YearMonth startMonth, YearMonth endMonth) {
        log.info("Fetching turnover records between {} and {}", startMonth, endMonth);

        return turnoverRepository.findAll().stream()
                .filter(t -> !t.getMonth().isBefore(startMonth) && !t.getMonth().isAfter(endMonth))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal calculateTotalDebitByHeader(Long headerId) {
        log.info("Calculating total debit for header ID: {}", headerId);

        List<MerchandiseTurnover> turnovers = turnoverRepository.findByHeaderId(headerId);

        return turnovers.stream()
                .map(MerchandiseTurnover::getDebitDisbursements)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateTotalCreditByHeader(Long headerId) {
        log.info("Calculating total credit for header ID: {}", headerId);

        List<MerchandiseTurnover> turnovers = turnoverRepository.findByHeaderId(headerId);

        return turnovers.stream()
                .map(MerchandiseTurnover::getCreditPrincipalRepayments)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateNetTurnoverByHeader(Long headerId) {
        log.info("Calculating net turnover for header ID: {}", headerId);

        BigDecimal totalDebit = calculateTotalDebitByHeader(headerId);
        BigDecimal totalCredit = calculateTotalCreditByHeader(headerId);

        return totalDebit.subtract(totalCredit);
    }

    // Helper methods
    private MerchandiseTurnoverDTO mapToDTO(MerchandiseTurnover turnover) {
        MerchandiseTurnoverDTO dto = new MerchandiseTurnoverDTO();
        dto.setId(turnover.getId());
        dto.setMonth(turnover.getMonth());
        dto.setDebitDisbursements(turnover.getDebitDisbursements());
        dto.setCreditPrincipalRepayments(turnover.getCreditPrincipalRepayments());
        dto.setHeaderId(turnover.getHeader().getId());
        return dto;
    }

    private void validateTurnover(MerchandiseTurnoverDTO dto) {
        if (dto.getMonth() == null) {
            throw new BadRequestException("Month is required");
        }
        if (dto.getDebitDisbursements() == null) {
            throw new BadRequestException("Debit disbursements is required");
        }
        if (dto.getDebitDisbursements().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Debit disbursements cannot be negative");
        }
        if (dto.getCreditPrincipalRepayments() == null) {
            throw new BadRequestException("Credit principal repayments is required");
        }
        if (dto.getCreditPrincipalRepayments().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Credit principal repayments cannot be negative");
        }
    }
}
