package com.cbo.credit_scoring.controllers;

import com.cbo.credit_scoring.dtos.CollateralRequestDTO;
import com.cbo.credit_scoring.dtos.CollateralResponseDTO;
import com.cbo.credit_scoring.dtos.CollateralSummaryDTO;
import com.cbo.credit_scoring.services.CollateralService;
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

@RestController
@RequestMapping("/api/v1/collateral")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Collateral", description = "APIs for managing collateral information")
public class CollateralController {

    private final CollateralService collateralService;

    // ============= Create Operations =============

    @Operation(summary = "Create collateral records")
    @PostMapping
    public ResponseEntity<CollateralResponseDTO> createCollateral(
            @Valid @RequestBody CollateralRequestDTO requestDTO) {
        return new ResponseEntity<>(collateralService.createCollateral(requestDTO), HttpStatus.CREATED);
    }

    // ============= Read Operations =============

    @Operation(summary = "Get all collateral for a case")
    @GetMapping("/case/{caseId}")
    public ResponseEntity<CollateralResponseDTO> getByCaseId(
            @Parameter(description = "Case ID", required = true)
            @PathVariable String caseId) {
        return ResponseEntity.ok(collateralService.getCollateralByCaseId(caseId));
    }

    @Operation(summary = "Get a single collateral record by ID")
    @GetMapping("/{id}")
    public ResponseEntity<CollateralResponseDTO.CollateralRecord> getById(
            @Parameter(description = "Collateral record ID", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(collateralService.getCollateralRecordById(id));
    }

    @Operation(summary = "Get all collateral records with pagination")
    @GetMapping
    public ResponseEntity<Page<CollateralResponseDTO.CollateralRecord>> getAll(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(collateralService.getAllCollateralRecords(pageable));
    }

    // ============= Update Operations =============

    @Operation(summary = "Update collateral for a case")
    @PutMapping("/case/{caseId}")
    public ResponseEntity<CollateralResponseDTO> updateCollateral(
            @Parameter(description = "Case ID", required = true)
            @PathVariable String caseId,
            @Valid @RequestBody CollateralRequestDTO requestDTO) {
        return ResponseEntity.ok(collateralService.updateCollateral(caseId, requestDTO));
    }

    // ============= Delete Operations =============

    @Operation(summary = "Delete all collateral for a case")
    @DeleteMapping("/case/{caseId}")
    public ResponseEntity<Void> deleteByCaseId(
            @Parameter(description = "Case ID", required = true)
            @PathVariable String caseId) {
        collateralService.deleteByCaseId(caseId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete a single collateral record by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @Parameter(description = "Collateral record ID", required = true)
            @PathVariable Long id) {
        collateralService.deleteCollateralRecord(id);
        return ResponseEntity.noContent().build();
    }

    // ============= Summary and Statistics =============

    @Operation(summary = "Get collateral summary for a case")
    @GetMapping("/summary/{caseId}")
    public ResponseEntity<CollateralSummaryDTO> getSummary(
            @Parameter(description = "Case ID", required = true)
            @PathVariable String caseId) {
        return ResponseEntity.ok(collateralService.getCollateralSummary(caseId));
    }

    @Operation(summary = "Calculate totals for a case")
    @GetMapping("/totals/{caseId}")
    public ResponseEntity<CollateralSummaryDTO> calculateTotals(
            @Parameter(description = "Case ID", required = true)
            @PathVariable String caseId) {
        return ResponseEntity.ok(collateralService.calculateTotals(caseId));
    }

    // ============= Health Check =============

    @Operation(summary = "Health check endpoint")
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Collateral Service is running");
    }
}