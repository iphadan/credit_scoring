package com.cbo.credit_scoring.controllers;

import com.cbo.credit_scoring.dtos.FinancialStatementRequestDTO;
import com.cbo.credit_scoring.dtos.FinancialStatementResponseDTO;
import com.cbo.credit_scoring.dtos.FinancialStatementSummaryDTO;
import com.cbo.credit_scoring.services.FinancialStatementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/financial-statements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Financial Statements", description = "Financial statement management APIs linked to cases")
public class FinancialStatementController {

    private final FinancialStatementService financialStatementService;

    // ============= Basic CRUD Operations =============

    @Operation(summary = "Create a new financial statement")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Financial statement created successfully",
                    content = @Content(schema = @Schema(implementation = FinancialStatementResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Case not found")
    })
    @PostMapping
    public ResponseEntity<FinancialStatementResponseDTO> createFinancialStatement(
            @Valid @RequestBody FinancialStatementRequestDTO requestDTO) {
        return new ResponseEntity<>(financialStatementService.createFinancialStatement(requestDTO),
                HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing financial statement")
    @PutMapping("/{id}")
    public ResponseEntity<FinancialStatementResponseDTO> updateFinancialStatement(
            @Parameter(description = "Financial statement ID", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody FinancialStatementRequestDTO requestDTO) {
        return ResponseEntity.ok(financialStatementService.updateFinancialStatement(id, requestDTO));
    }

    @Operation(summary = "Get financial statement by ID")
    @GetMapping("/{id}")
    public ResponseEntity<FinancialStatementResponseDTO> getFinancialStatementById(
            @Parameter(description = "Financial statement ID", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(financialStatementService.getFinancialStatementById(id));
    }

    @Operation(summary = "Get all financial statements with pagination")
    @GetMapping
    public ResponseEntity<Page<FinancialStatementResponseDTO>> getAllFinancialStatements(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(financialStatementService.getAllFinancialStatements(pageable));
    }

    @Operation(summary = "Delete a financial statement")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFinancialStatement(
            @Parameter(description = "Financial statement ID", example = "1", required = true)
            @PathVariable Long id) {
        financialStatementService.deleteFinancialStatement(id);
        return ResponseEntity.noContent().build();
    }

    // ============= Case-based Operations =============

    @Operation(summary = "Get all financial statements for a case")
    @GetMapping("/by-case/{caseId}")
    public ResponseEntity<List<FinancialStatementResponseDTO>> getFinancialStatementsByCaseId(
            @Parameter(description = "Case ID", example = "CASE-001", required = true)
            @PathVariable String caseId) {
        return ResponseEntity.ok(financialStatementService.getFinancialStatementsByCaseId(caseId));
    }

    @Operation(summary = "Get the latest financial statement for a case")
    @GetMapping("/by-case/{caseId}/latest")
    public ResponseEntity<FinancialStatementResponseDTO> getLatestFinancialStatementByCaseId(
            @Parameter(description = "Case ID", example = "CASE-001", required = true)
            @PathVariable String caseId) {
        return ResponseEntity.ok(financialStatementService.getLatestFinancialStatementByCaseId(caseId));
    }

    @Operation(summary = "Get a specific version of financial statement for a case")
    @GetMapping("/by-case/{caseId}/version/{version}")
    public ResponseEntity<FinancialStatementResponseDTO> getFinancialStatementByCaseIdAndVersion(
            @Parameter(description = "Case ID", example = "CASE-001", required = true)
            @PathVariable String caseId,
            @Parameter(description = "Version number", example = "1", required = true)
            @PathVariable Integer version) {
        return ResponseEntity.ok(financialStatementService.getFinancialStatementByCaseIdAndVersion(caseId, version));
    }

    // ============= Search Operations =============

    @Operation(summary = "Search financial statements by company name")
    @GetMapping("/search/by-company")
    public ResponseEntity<List<FinancialStatementResponseDTO>> searchByCompanyName(
            @Parameter(description = "Company name to search", example = "ABC Corp", required = true)
            @RequestParam String companyName) {
        return ResponseEntity.ok(financialStatementService.searchByCompanyName(companyName));
    }

    @Operation(summary = "Get financial statements by date range")
    @GetMapping("/search/by-date-range")
    public ResponseEntity<List<FinancialStatementResponseDTO>> getFinancialStatementsByDateRange(
            @Parameter(description = "Start date (ISO format: yyyy-MM-dd)", example = "2024-01-01", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (ISO format: yyyy-MM-dd)", example = "2024-12-31", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(financialStatementService.getFinancialStatementsByDateRange(startDate, endDate));
    }

    @Operation(summary = "Get financial statements by type")
    @GetMapping("/search/by-type/{statementType}")
    public ResponseEntity<List<FinancialStatementResponseDTO>> getFinancialStatementsByType(
            @Parameter(description = "Statement type", example = "COMPREHENSIVE", required = true)
            @PathVariable String statementType) {
        return ResponseEntity.ok(financialStatementService.getFinancialStatementsByType(statementType));
    }

    // ============= Analysis and Summary =============

    @Operation(summary = "Get comprehensive summary of a financial statement")
    @GetMapping("/{id}/summary")
    public ResponseEntity<FinancialStatementSummaryDTO> getFinancialStatementSummary(
            @Parameter(description = "Financial statement ID", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(financialStatementService.getFinancialStatementSummary(id));
    }

    @Operation(summary = "Compare two financial statements")
    @GetMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareFinancialStatements(
            @Parameter(description = "First financial statement ID", example = "1", required = true)
            @RequestParam Long id1,
            @Parameter(description = "Second financial statement ID", example = "2", required = true)
            @RequestParam Long id2) {
        return ResponseEntity.ok(financialStatementService.compareFinancialStatements(id1, id2));
    }

    @Operation(summary = "Generate ratio analysis report")
    @GetMapping("/{id}/ratio-analysis")
    public ResponseEntity<Map<String, Object>> generateRatioAnalysis(
            @Parameter(description = "Financial statement ID", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(financialStatementService.generateRatioAnalysis(id));
    }

    @Operation(summary = "Get trend analysis for key metrics")
    @GetMapping("/{id}/trend-analysis")
    public ResponseEntity<Map<String, List<BigDecimal>>> getTrendAnalysis(
            @Parameter(description = "Financial statement ID", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "List of metrics to analyze", example = "revenue,netIncome,assets")
            @RequestParam List<String> metrics) {
        return ResponseEntity.ok(financialStatementService.getTrendAnalysis(id, metrics));
    }

    @Operation(summary = "Export financial statement data")
    @GetMapping("/{id}/export")
    public ResponseEntity<Map<String, Object>> exportFinancialStatementData(
            @Parameter(description = "Financial statement ID", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(financialStatementService.exportFinancialStatementData(id));
    }

    // ============= Validation Operations =============

    @Operation(summary = "Validate financial statement data")
    @GetMapping("/{id}/validate")
    public ResponseEntity<Map<String, Object>> validateFinancialStatement(
            @Parameter(description = "Financial statement ID", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(financialStatementService.validateFinancialStatement(id));
    }

    @Operation(summary = "Check if balance sheet balances")
    @GetMapping("/{id}/is-balanced")
    public ResponseEntity<Map<String, Boolean>> isBalanceSheetBalanced(
            @Parameter(description = "Financial statement ID", example = "1", required = true)
            @PathVariable Long id) {
        boolean isBalanced = financialStatementService.isBalanceSheetBalanced(id);
        return ResponseEntity.ok(Map.of("balanced", isBalanced));
    }

    // ============= Batch Operations =============

    @Operation(summary = "Delete all financial statements for a case")
    @DeleteMapping("/by-case/{caseId}/delete-all")
    public ResponseEntity<Map<String, Object>> deleteAllFinancialStatementsByCase(
            @Parameter(description = "Case ID", example = "CASE-001", required = true)
            @PathVariable String caseId) {
        List<FinancialStatementResponseDTO> statements =
                financialStatementService.getFinancialStatementsByCaseId(caseId);
        statements.forEach(s -> financialStatementService.deleteFinancialStatement(s.getId()));

        return ResponseEntity.ok(Map.of(
                "message", "All financial statements deleted for case: " + caseId,
                "count", statements.size()
        ));
    }

    // ============= Health Check =============

    @Operation(summary = "Health check endpoint")
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Financial Statement Service",
                "timestamp", LocalDate.now().toString()
        ));
    }
}