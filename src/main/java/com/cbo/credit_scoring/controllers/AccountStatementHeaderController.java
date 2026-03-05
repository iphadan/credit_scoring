package com.cbo.credit_scoring.controllers;

import com.cbo.credit_scoring.dtos.AccountStatementHeaderDTO;
import com.cbo.credit_scoring.dtos.AccountStatementDTO;
import com.cbo.credit_scoring.dtos.AccountStatementSummaryDTO;
import com.cbo.credit_scoring.services.AccountStatementHeaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/account-statement/headers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AccountStatementHeaderController {

    private final AccountStatementHeaderService headerService;

    // ============= Header CRUD Operations =============

    @PostMapping
    public ResponseEntity<AccountStatementHeaderDTO> createHeader(
             @RequestBody AccountStatementHeaderDTO headerDTO) {
        return new ResponseEntity<>(headerService.createHeader(headerDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountStatementHeaderDTO> updateHeader(
            @PathVariable Long id,
             @RequestBody AccountStatementHeaderDTO headerDTO) {
        return ResponseEntity.ok(headerService.updateHeader(id, headerDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountStatementHeaderDTO> getHeaderById(@PathVariable Long id) {
        return ResponseEntity.ok(headerService.getHeaderById(id));
    }

    @GetMapping("/caseId/{caseId}")
    public ResponseEntity<AccountStatementHeaderDTO> getHeaderByCaseId(@PathVariable String caseId) {
        return ResponseEntity.ok(headerService.getHeaderByCaseId(caseId));
    }

    @GetMapping
    public ResponseEntity<Page<AccountStatementHeaderDTO>> getAllHeaders(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(headerService.getAllHeaders(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHeader(@PathVariable Long id) {
        headerService.deleteHeader(id);
        return ResponseEntity.noContent().build();
    }

    // ============= Search Operations =============

    @GetMapping("/search")
    public ResponseEntity<List<AccountStatementHeaderDTO>> searchHeaders(@RequestParam String term) {
        return ResponseEntity.ok(headerService.searchHeaders(term));
    }

    @GetMapping("/search/by-account-holder")
    public ResponseEntity<List<AccountStatementHeaderDTO>> searchByAccountHolder(
            @RequestParam String accountHolder) {
        return ResponseEntity.ok(headerService.searchByAccountHolder(accountHolder));
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<AccountStatementHeaderDTO> getHeaderByAccountNumber(
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(headerService.getHeaderByAccountNumber(accountNumber));
    }

    @GetMapping("/industry/{industry}")
    public ResponseEntity<List<AccountStatementHeaderDTO>> getHeadersByIndustry(
            @PathVariable String industry) {
        return ResponseEntity.ok(headerService.getHeadersByIndustry(industry));
    }

    @GetMapping("/facility-type/{facilityType}")
    public ResponseEntity<List<AccountStatementHeaderDTO>> getHeadersByFacilityType(
            @PathVariable String facilityType) {
        return ResponseEntity.ok(headerService.getHeadersByFacilityType(facilityType));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AccountStatementHeaderDTO>> getHeadersByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(headerService.getHeadersByStatus(status));
    }

    @GetMapping("/approved-date-range")
    public ResponseEntity<List<AccountStatementHeaderDTO>> getHeadersByApprovedDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(headerService.getHeadersByApprovedDateRange(startDate, endDate));
    }

    @GetMapping("/date-opened-range")
    public ResponseEntity<List<AccountStatementHeaderDTO>> getHeadersByDateAccountOpenedRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(headerService.getHeadersByDateAccountOpenedRange(startDate, endDate));
    }

    @GetMapping("/report-date/{reportDate}")
    public ResponseEntity<List<AccountStatementHeaderDTO>> getHeadersByReportDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate) {
        return ResponseEntity.ok(headerService.getHeadersByReportDate(reportDate));
    }

    // ============= Statement Record Management =============

    @PostMapping("/{headerId}/statements")
    public ResponseEntity<AccountStatementHeaderDTO> addStatementRecord(
            @PathVariable Long headerId,
             @RequestBody AccountStatementDTO statementDTO) {
        return new ResponseEntity<>(headerService.addStatementRecord(headerId, statementDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{headerId}/statements/{statementId}")
    public ResponseEntity<AccountStatementHeaderDTO> updateStatementRecord(
            @PathVariable Long headerId,
            @PathVariable Long statementId,
             @RequestBody AccountStatementDTO statementDTO) {
        return ResponseEntity.ok(headerService.updateStatementRecord(headerId, statementId, statementDTO));
    }

    @DeleteMapping("/{headerId}/statements/{statementId}")
    public ResponseEntity<AccountStatementHeaderDTO> removeStatementRecord(
            @PathVariable Long headerId,
            @PathVariable Long statementId) {
        return ResponseEntity.ok(headerService.removeStatementRecord(headerId, statementId));
    }

    // ============= Summary and Statistics =============

    @GetMapping("/{headerId}/summary")
    public ResponseEntity<AccountStatementSummaryDTO> getHeaderSummary(@PathVariable Long headerId) {
        return ResponseEntity.ok(headerService.getHeaderSummary(headerId));
    }

    @GetMapping("/summary/all")
    public ResponseEntity<List<AccountStatementSummaryDTO>> getAllHeadersSummary() {
        return ResponseEntity.ok(headerService.getAllHeadersSummary());
    }

    @GetMapping("/{headerId}/statistics")
    public ResponseEntity<Map<String, Object>> getHeaderStatistics(@PathVariable Long headerId) {
        return ResponseEntity.ok(headerService.getHeaderStatistics(headerId));
    }

    @GetMapping("/{headerId}/totals/credit")
    public ResponseEntity<BigDecimal> getTotalCredit(@PathVariable Long headerId) {
        return ResponseEntity.ok(headerService.calculateTotalCredit(headerId));
    }

    @GetMapping("/{headerId}/totals/debit")
    public ResponseEntity<BigDecimal> getTotalDebit(@PathVariable Long headerId) {
        return ResponseEntity.ok(headerService.calculateTotalDebit(headerId));
    }

    @GetMapping("/{headerId}/totals/net")
    public ResponseEntity<BigDecimal> getNetTurnover(@PathVariable Long headerId) {
        return ResponseEntity.ok(headerService.calculateNetTurnover(headerId));
    }

    @GetMapping("/{headerId}/utilization/average")
    public ResponseEntity<BigDecimal> getAverageUtilization(@PathVariable Long headerId) {
        return ResponseEntity.ok(headerService.calculateAverageUtilization(headerId));
    }

    @GetMapping("/{headerId}/utilization/max")
    public ResponseEntity<BigDecimal> getMaxUtilization(@PathVariable Long headerId) {
        return ResponseEntity.ok(headerService.calculateMaxUtilization(headerId));
    }

    @GetMapping("/{headerId}/utilization/min")
    public ResponseEntity<BigDecimal> getMinUtilization(@PathVariable Long headerId) {
        return ResponseEntity.ok(headerService.calculateMinUtilization(headerId));
    }

    // ============= Trends and Analytics =============

    @GetMapping("/{headerId}/trends/utilization")
    public ResponseEntity<Map<YearMonth, BigDecimal>> getUtilizationTrend(@PathVariable Long headerId) {
        return ResponseEntity.ok(headerService.getUtilizationTrend(headerId));
    }

    @GetMapping("/{headerId}/trends/credit")
    public ResponseEntity<Map<YearMonth, BigDecimal>> getCreditTrend(@PathVariable Long headerId) {
        return ResponseEntity.ok(headerService.getCreditTrend(headerId));
    }

    @GetMapping("/{headerId}/trends/debit")
    public ResponseEntity<Map<YearMonth, BigDecimal>> getDebitTrend(@PathVariable Long headerId) {
        return ResponseEntity.ok(headerService.getDebitTrend(headerId));
    }

    @GetMapping("/{headerId}/months-exceeding-threshold")
    public ResponseEntity<List<AccountStatementDTO>> getMonthsExceedingThreshold(
            @PathVariable Long headerId,
            @RequestParam(defaultValue = "80") BigDecimal threshold) {
        return ResponseEntity.ok(headerService.getMonthsExceedingThreshold(headerId, threshold));
    }

    // ============= Utility Endpoints =============

    @GetMapping("/exists/account/{accountNumber}")
    public ResponseEntity<Boolean> checkAccountNumberExists(@PathVariable String accountNumber) {
        try {
            headerService.getHeaderByAccountNumber(accountNumber);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/count/by-status/{status}")
    public ResponseEntity<Long> countByStatus(@PathVariable String status) {
        return ResponseEntity.ok((long) headerService.getHeadersByStatus(status).size());
    }
}