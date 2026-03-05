package com.cbo.credit_scoring.controllers;

import com.cbo.credit_scoring.dtos.OdSwingHeaderDTO;
import com.cbo.credit_scoring.dtos.OdSwingDTO;
import com.cbo.credit_scoring.dtos.OdSwingSummaryDTO;
import com.cbo.credit_scoring.services.OdSwingHeaderService;
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
@RequestMapping("/api/v1/od-swing/headers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OdSwingHeaderController {

    private final OdSwingHeaderService headerService;

    @PostMapping
    public ResponseEntity<OdSwingHeaderDTO> createHeader( @RequestBody OdSwingHeaderDTO headerDTO) {
        return new ResponseEntity<>(headerService.createHeader(headerDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OdSwingHeaderDTO> updateHeader(
            @PathVariable Long id,
             @RequestBody OdSwingHeaderDTO headerDTO) {
        return ResponseEntity.ok(headerService.updateHeader(id, headerDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OdSwingHeaderDTO> getHeaderById(@PathVariable Long id) {
        return ResponseEntity.ok(headerService.getHeaderById(id));
    }

    @GetMapping("/caseId/{caseId}")
    public ResponseEntity<OdSwingHeaderDTO> getHeaderByCaseId(@PathVariable String caseId) {
        return ResponseEntity.ok(headerService.getHeaderByCaseId(caseId));
    }

    @GetMapping
    public ResponseEntity<Page<OdSwingHeaderDTO>> getAllHeaders(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(headerService.getAllHeaders(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHeader(@PathVariable Long id) {
        headerService.deleteHeader(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<OdSwingHeaderDTO>> searchHeaders(@RequestParam String term) {
        return ResponseEntity.ok(headerService.searchHeaders(term));
    }

    @GetMapping("/search/by-account-holder")
    public ResponseEntity<List<OdSwingHeaderDTO>> searchByAccountHolder(@RequestParam String accountHolder) {
        return ResponseEntity.ok(headerService.searchByAccountHolder(accountHolder));
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<OdSwingHeaderDTO> getHeaderByAccountNumber(@PathVariable String accountNumber) {
        return ResponseEntity.ok(headerService.getHeaderByAccountNumber(accountNumber));
    }

    @GetMapping("/industry/{industry}")
    public ResponseEntity<List<OdSwingHeaderDTO>> getHeadersByIndustry(@PathVariable String industry) {
        return ResponseEntity.ok(headerService.getHeadersByIndustry(industry));
    }

    @GetMapping("/facility-type/{facilityType}")
    public ResponseEntity<List<OdSwingHeaderDTO>> getHeadersByFacilityType(@PathVariable String facilityType) {
        return ResponseEntity.ok(headerService.getHeadersByFacilityType(facilityType));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OdSwingHeaderDTO>> getHeadersByStatus(@PathVariable String status) {
        return ResponseEntity.ok(headerService.getHeadersByStatus(status));
    }

    @GetMapping("/approved-date-range")
    public ResponseEntity<List<OdSwingHeaderDTO>> getHeadersByApprovedDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(headerService.getHeadersByApprovedDateRange(startDate, endDate));
    }

    @GetMapping("/report-date/{reportDate}")
    public ResponseEntity<List<OdSwingHeaderDTO>> getHeadersByReportDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate) {
        return ResponseEntity.ok(headerService.getHeadersByReportDate(reportDate));
    }

    // Swing record management endpoints
    @PostMapping("/{headerId}/swings")
    public ResponseEntity<OdSwingHeaderDTO> addSwingRecord(
            @PathVariable Long headerId,
             @RequestBody OdSwingDTO swingDTO) {
        return new ResponseEntity<>(headerService.addSwingRecord(headerId, swingDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{headerId}/swings/{swingId}")
    public ResponseEntity<OdSwingHeaderDTO> updateSwingRecord(
            @PathVariable Long headerId,
            @PathVariable Long swingId,
             @RequestBody OdSwingDTO swingDTO) {
        return ResponseEntity.ok(headerService.updateSwingRecord(headerId, swingId, swingDTO));
    }

    @DeleteMapping("/{headerId}/swings/{swingId}")
    public ResponseEntity<OdSwingHeaderDTO> removeSwingRecord(
            @PathVariable Long headerId,
            @PathVariable Long swingId) {
        return ResponseEntity.ok(headerService.removeSwingRecord(headerId, swingId));
    }

    // Summary and statistics endpoints
    @GetMapping("/{headerId}/summary")
    public ResponseEntity<OdSwingSummaryDTO> getHeaderSummary(@PathVariable Long headerId) {
        return ResponseEntity.ok(headerService.getHeaderSummary(headerId));
    }

    @GetMapping("/summary/all")
    public ResponseEntity<List<OdSwingSummaryDTO>> getAllHeadersSummary() {
        return ResponseEntity.ok(headerService.getAllHeadersSummary());
    }

    @GetMapping("/{headerId}/statistics")
    public ResponseEntity<Map<String, Object>> getHeaderStatistics(@PathVariable Long headerId) {
        return ResponseEntity.ok(headerService.getHeaderStatistics(headerId));
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

    @GetMapping("/{headerId}/swing-range/average")
    public ResponseEntity<BigDecimal> getAverageSwingRange(@PathVariable Long headerId) {
        return ResponseEntity.ok(headerService.calculateAverageSwingRange(headerId));
    }

    @GetMapping("/{headerId}/utilization/trend")
    public ResponseEntity<Map<YearMonth, BigDecimal>> getUtilizationTrend(@PathVariable Long headerId) {
        return ResponseEntity.ok(headerService.getUtilizationTrend(headerId));
    }

    @GetMapping("/{headerId}/swing-range/trend")
    public ResponseEntity<Map<YearMonth, BigDecimal>> getSwingRangeTrend(@PathVariable Long headerId) {
        return ResponseEntity.ok(headerService.getSwingRangeTrend(headerId));
    }

    @GetMapping("/{headerId}/months-exceeding-threshold")
    public ResponseEntity<List<OdSwingDTO>> getMonthsExceedingThreshold(
            @PathVariable Long headerId,
            @RequestParam(defaultValue = "80") BigDecimal threshold) {
        return ResponseEntity.ok(headerService.getMonthsExceedingThreshold(headerId, threshold));
    }
}