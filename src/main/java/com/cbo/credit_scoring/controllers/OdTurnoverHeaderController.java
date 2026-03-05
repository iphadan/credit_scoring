package com.cbo.credit_scoring.controllers;

import com.cbo.credit_scoring.dtos.OdTurnoverHeaderDTO;
import com.cbo.credit_scoring.dtos.OdTurnoverDTO;
import com.cbo.credit_scoring.dtos.OdTurnoverSummaryDTO;
import com.cbo.credit_scoring.services.OdTurnoverHeaderService;
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
@RequestMapping("/api/v1/od/headers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OdTurnoverHeaderController {

    private final OdTurnoverHeaderService headerService;

    @PostMapping
    public ResponseEntity<OdTurnoverHeaderDTO> createHeader( @RequestBody OdTurnoverHeaderDTO headerDTO) {
        return new ResponseEntity<>(headerService.createHeader(headerDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OdTurnoverHeaderDTO> updateHeader(
            @PathVariable Long id,
             @RequestBody OdTurnoverHeaderDTO headerDTO) {
        return ResponseEntity.ok(headerService.updateHeader(id, headerDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OdTurnoverHeaderDTO> getHeaderById(@PathVariable Long id) {
        return ResponseEntity.ok(headerService.getHeaderById(id));
    }

    @GetMapping("/caseId/{caseId}")
    public ResponseEntity<OdTurnoverHeaderDTO> getHeaderByCaseId(@PathVariable String caseId) {
        return ResponseEntity.ok(headerService.getHeaderByCaseId(caseId));
    }

    @GetMapping
    public ResponseEntity<Page<OdTurnoverHeaderDTO>> getAllHeaders(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(headerService.getAllHeaders(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHeader(@PathVariable Long id) {
        headerService.deleteHeader(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<OdTurnoverHeaderDTO>> searchHeaders(@RequestParam String term) {
        return ResponseEntity.ok(headerService.searchHeaders(term));
    }

    @GetMapping("/search/by-account-holder")
    public ResponseEntity<List<OdTurnoverHeaderDTO>> searchByAccountHolder(@RequestParam String accountHolder) {
        return ResponseEntity.ok(headerService.searchByAccountHolder(accountHolder));
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<OdTurnoverHeaderDTO> getHeaderByAccountNumber(@PathVariable String accountNumber) {
        return ResponseEntity.ok(headerService.getHeaderByAccountNumber(accountNumber));
    }

    @GetMapping("/industry/{industry}")
    public ResponseEntity<List<OdTurnoverHeaderDTO>> getHeadersByIndustry(@PathVariable String industry) {
        return ResponseEntity.ok(headerService.getHeadersByIndustry(industry));
    }

    @GetMapping("/facility-type/{facilityType}")
    public ResponseEntity<List<OdTurnoverHeaderDTO>> getHeadersByFacilityType(@PathVariable String facilityType) {
        return ResponseEntity.ok(headerService.getHeadersByFacilityType(facilityType));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OdTurnoverHeaderDTO>> getHeadersByStatus(@PathVariable String status) {
        return ResponseEntity.ok(headerService.getHeadersByStatus(status));
    }

    @GetMapping("/approved-date-range")
    public ResponseEntity<List<OdTurnoverHeaderDTO>> getHeadersByApprovedDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(headerService.getHeadersByApprovedDateRange(startDate, endDate));
    }

    @GetMapping("/report-date/{reportDate}")
    public ResponseEntity<List<OdTurnoverHeaderDTO>> getHeadersByReportDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate) {
        return ResponseEntity.ok(headerService.getHeadersByReportDate(reportDate));
    }

    // Turnover record management endpoints
    @PostMapping("/{headerId}/turnovers")
    public ResponseEntity<OdTurnoverHeaderDTO> addTurnoverRecord(
            @PathVariable Long headerId,
             @RequestBody OdTurnoverDTO turnoverDTO) {
        return new ResponseEntity<>(headerService.addTurnoverRecord(headerId, turnoverDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{headerId}/turnovers/{turnoverId}")
    public ResponseEntity<OdTurnoverHeaderDTO> updateTurnoverRecord(
            @PathVariable Long headerId,
            @PathVariable Long turnoverId,
             @RequestBody OdTurnoverDTO turnoverDTO) {
        return ResponseEntity.ok(headerService.updateTurnoverRecord(headerId, turnoverId, turnoverDTO));
    }

    @DeleteMapping("/{headerId}/turnovers/{turnoverId}")
    public ResponseEntity<OdTurnoverHeaderDTO> removeTurnoverRecord(
            @PathVariable Long headerId,
            @PathVariable Long turnoverId) {
        return ResponseEntity.ok(headerService.removeTurnoverRecord(headerId, turnoverId));
    }

    // Summary and statistics endpoints
    @GetMapping("/{headerId}/summary")
    public ResponseEntity<OdTurnoverSummaryDTO> getHeaderSummary(@PathVariable Long headerId) {
        return ResponseEntity.ok(headerService.getHeaderSummary(headerId));
    }

    @GetMapping("/summary/all")
    public ResponseEntity<List<OdTurnoverSummaryDTO>> getAllHeadersSummary() {
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

    @GetMapping("/{headerId}/utilization/trend")
    public ResponseEntity<Map<YearMonth, BigDecimal>> getUtilizationTrend(@PathVariable Long headerId) {
        return ResponseEntity.ok(headerService.getUtilizationTrend(headerId));
    }
}