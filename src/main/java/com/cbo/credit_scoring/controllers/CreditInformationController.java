package com.cbo.credit_scoring.controllers;

import com.cbo.credit_scoring.dtos.CreditInformationRequestDTO;
import com.cbo.credit_scoring.dtos.CreditInformationResponseDTO;
import com.cbo.credit_scoring.dtos.CreditSummaryDTO;
import com.cbo.credit_scoring.models.enums.BankType;
import com.cbo.credit_scoring.services.CreditInformationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/credit-information")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Credit Information", description = "APIs for managing credit information (Coop Bank & Other Banks)")
public class CreditInformationController {

    private final CreditInformationService creditService;

    // ============= Create Operations =============

    @Operation(summary = "Create credit information records")
    @PostMapping
    public ResponseEntity<List<CreditInformationResponseDTO>> createCreditInformation(
            @Valid @RequestBody CreditInformationRequestDTO requestDTO) {
        return new ResponseEntity<>(creditService.createCreditInformation(requestDTO), HttpStatus.CREATED);
    }

    // ============= Read Operations =============

    @Operation(summary = "Get all credit information for a case")
    @GetMapping("/case/{caseId}")
    public ResponseEntity<List<CreditInformationResponseDTO>> getByCaseId(
            @Parameter(description = "Case ID", required = true)
            @PathVariable String caseId) {
        return ResponseEntity.ok(creditService.getCreditInformationByCaseId(caseId));
    }

    @Operation(summary = "Get credit information for a case by bank type")
    @GetMapping("/case/{caseId}/bank/{bankType}")
    public ResponseEntity<CreditInformationResponseDTO> getByCaseIdAndBankType(
            @Parameter(description = "Case ID", required = true)
            @PathVariable String caseId,
            @Parameter(description = "Bank Type (COOP_BANK or OTHER_BANK)", required = true)
            @PathVariable BankType bankType) {
        return ResponseEntity.ok(creditService.getCreditInformationByCaseIdAndBankType(caseId, bankType));
    }

    @Operation(summary = "Get a single credit record by ID")
    @GetMapping("/{id}")
    public ResponseEntity<CreditInformationResponseDTO> getById(
            @Parameter(description = "Credit record ID", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(creditService.getCreditRecordById(id));
    }

    @Operation(summary = "Get all credit records with pagination")
    @GetMapping
    public ResponseEntity<Page<CreditInformationResponseDTO>> getAll(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(creditService.getAllCreditRecords(pageable));
    }

    // ============= Update Operations =============

    @Operation(summary = "Update credit information for a case and bank type")
    @PutMapping("/case/{caseId}/bank/{bankType}")
    public ResponseEntity<List<CreditInformationResponseDTO>> updateCreditInformation(
            @Parameter(description = "Case ID", required = true)
            @PathVariable String caseId,
            @Parameter(description = "Bank Type (COOP_BANK or OTHER_BANK)", required = true)
            @PathVariable BankType bankType,
            @Valid @RequestBody CreditInformationRequestDTO requestDTO) {
        return ResponseEntity.ok(creditService.updateCreditInformation(caseId, bankType, requestDTO));
    }

    // ============= Delete Operations =============

    @Operation(summary = "Delete all credit information for a case")
    @DeleteMapping("/case/{caseId}")
    public ResponseEntity<Void> deleteByCaseId(
            @Parameter(description = "Case ID", required = true)
            @PathVariable String caseId) {
        creditService.deleteByCaseId(caseId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete credit information for a case by bank type")
    @DeleteMapping("/case/{caseId}/bank/{bankType}")
    public ResponseEntity<Void> deleteByCaseIdAndBankType(
            @Parameter(description = "Case ID", required = true)
            @PathVariable String caseId,
            @Parameter(description = "Bank Type (COOP_BANK or OTHER_BANK)", required = true)
            @PathVariable BankType bankType) {
        creditService.deleteByCaseIdAndBankType(caseId, bankType);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete a single credit record by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @Parameter(description = "Credit record ID", required = true)
            @PathVariable Long id) {
        creditService.deleteCreditRecord(id);
        return ResponseEntity.noContent().build();
    }

    // ============= Summary and Statistics =============

    @Operation(summary = "Get credit summary for a case")
    @GetMapping("/summary/{caseId}")
    public ResponseEntity<CreditSummaryDTO> getCreditSummary(
            @Parameter(description = "Case ID", required = true)
            @PathVariable String caseId) {
        return ResponseEntity.ok(creditService.getCreditSummary(caseId));
    }

    @Operation(summary = "Calculate totals for a case")
    @GetMapping("/totals/{caseId}")
    public ResponseEntity<CreditSummaryDTO> calculateTotals(
            @Parameter(description = "Case ID", required = true)
            @PathVariable String caseId) {
        return ResponseEntity.ok(creditService.calculateTotals(caseId));
    }

    // ============= Validation =============

    @Operation(summary = "Validate credit information for a case")
    @GetMapping("/validate/{caseId}")
    public ResponseEntity<Boolean> validateCreditInformation(
            @Parameter(description = "Case ID", required = true)
            @PathVariable String caseId) {
        return ResponseEntity.ok(creditService.validateCreditInformation(caseId));
    }

    // ============= Health Check =============

    @Operation(summary = "Health check endpoint")
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Credit Information Service is running");
    }
}