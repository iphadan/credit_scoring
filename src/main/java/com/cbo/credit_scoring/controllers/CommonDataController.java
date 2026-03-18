package com.cbo.credit_scoring.controllers;

import com.cbo.credit_scoring.dtos.CaseAggregateDTO;
import com.cbo.credit_scoring.dtos.CaseSummaryDTO;
import com.cbo.credit_scoring.services.CommonDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/common")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Common Data", description = "Unified APIs to fetch all data for a caseId from all modules")
public class CommonDataController {

    private final CommonDataService commonDataService;

    @Operation(summary = "Get all data for a caseId from all modules")
    @GetMapping("/case/{caseId}")
    public ResponseEntity<CaseAggregateDTO> getAllDataByCaseId(
            @Parameter(description = "Case ID", required = true)
            @PathVariable String caseId) {
        return ResponseEntity.ok(commonDataService.getAllDataByCaseId(caseId));
    }

    @Operation(summary = "Get summary statistics for a case")
    @GetMapping("/case/{caseId}/summary")
    public ResponseEntity<CaseSummaryDTO> getCaseSummary(
            @Parameter(description = "Case ID", required = true)
            @PathVariable String caseId) {
        return ResponseEntity.ok(commonDataService.getCaseSummary(caseId));
    }

    @Operation(summary = "Check if a case exists in any module")
    @GetMapping("/case/{caseId}/exists")
    public ResponseEntity<Map<String, Boolean>> checkCaseExists(
            @Parameter(description = "Case ID", required = true)
            @PathVariable String caseId) {
        boolean exists = commonDataService.caseExists(caseId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @Operation(summary = "Get all unique caseIds across all modules")
    @GetMapping("/cases")
    public ResponseEntity<List<String>> getAllCaseIds() {
        return ResponseEntity.ok(commonDataService.getAllCaseIds());
    }

    @Operation(summary = "Delete all data for a caseId from all modules")
    @DeleteMapping("/case/{caseId}")
    public ResponseEntity<Void> deleteAllDataByCaseId(
            @Parameter(description = "Case ID", required = true)
            @PathVariable String caseId) {
        commonDataService.deleteAllDataByCaseId(caseId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get module availability for a case")
    @GetMapping("/case/{caseId}/modules")
    public ResponseEntity<Map<String, Boolean>> getModuleAvailability(
            @Parameter(description = "Case ID", required = true)
            @PathVariable String caseId) {
        CaseSummaryDTO summary = commonDataService.getCaseSummary(caseId);
        return ResponseEntity.ok(summary.getModulesAvailable());
    }

    @Operation(summary = "Get counts by module for a case")
    @GetMapping("/case/{caseId}/counts")
    public ResponseEntity<Map<String, Integer>> getModuleCounts(
            @Parameter(description = "Case ID", required = true)
            @PathVariable String caseId) {
        CaseSummaryDTO summary = commonDataService.getCaseSummary(caseId);
        return ResponseEntity.ok(Map.of(
                "preShipment", summary.getPreShipmentCount(),
                "odTurnover", summary.getOdTurnoverCount(),
                "odSwing", summary.getOdSwingCount(),
                "merchandise", summary.getMerchandiseCount(),
                "accountStatement", summary.getAccountStatementCount(),
                "financialStatement", summary.getFinancialStatementCount(),
                "creditInformation", summary.getCreditInformationCount(),
                "collateral", summary.getCollateralCount()
        ));
    }

    @Operation(summary = "Get latest reporting dates by module for a case")
    @GetMapping("/case/{caseId}/dates")
    public ResponseEntity<Map<String, String>> getLatestReportingDates(
            @Parameter(description = "Case ID", required = true)
            @PathVariable String caseId) {
        CaseSummaryDTO summary = commonDataService.getCaseSummary(caseId);
        return ResponseEntity.ok(summary.getLatestReportingDates());
    }

    @Operation(summary = "Health check endpoint")
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Common Data Service",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}