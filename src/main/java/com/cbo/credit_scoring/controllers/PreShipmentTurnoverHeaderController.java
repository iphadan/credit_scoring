package com.cbo.credit_scoring.controllers;

import com.cbo.credit_scoring.dtos.PreShipmentTurnoverHeaderDTO;
import com.cbo.credit_scoring.dtos.PreShipmentTurnoverDTO;
import com.cbo.credit_scoring.services.PreShipmentTurnoverHeaderService;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pre-shipment/headers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PreShipmentTurnoverHeaderController {

    private final PreShipmentTurnoverHeaderService headerService;

    @PostMapping
    public ResponseEntity<PreShipmentTurnoverHeaderDTO> createHeader( @RequestBody PreShipmentTurnoverHeaderDTO headerDTO) {
        PreShipmentTurnoverDTO headerDTO1 = null;
        return new ResponseEntity<>(headerService.createHeader(headerDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PreShipmentTurnoverHeaderDTO> updateHeader(
            @PathVariable Long id,
             @RequestBody PreShipmentTurnoverHeaderDTO headerDTO) {
        return ResponseEntity.ok(headerService.updateHeader(id, headerDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PreShipmentTurnoverHeaderDTO> getHeaderById(@PathVariable Long id) {
        return ResponseEntity.ok(headerService.getHeaderById(id));
    }

    @GetMapping("/caseId/{caseId}")
    public ResponseEntity<PreShipmentTurnoverHeaderDTO> getHeaderByCaseId(@PathVariable String caseId) {
        return ResponseEntity.ok(headerService.getHeaderByCaseId(caseId));
    }

    @GetMapping
    public ResponseEntity<Page<PreShipmentTurnoverHeaderDTO>> getAllHeaders(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(headerService.getAllHeaders(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHeader(@PathVariable Long id) {
        headerService.deleteHeader(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<PreShipmentTurnoverHeaderDTO>> searchHeaders(@RequestParam String term) {
        return ResponseEntity.ok(headerService.searchHeaders(term));
    }

    @GetMapping("/search/by-customer")
    public ResponseEntity<List<PreShipmentTurnoverHeaderDTO>> searchByCustomerName(@RequestParam String customerName) {
        return ResponseEntity.ok(headerService.searchByCustomerName(customerName));
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<PreShipmentTurnoverHeaderDTO> getHeaderByAccountNumber(@PathVariable String accountNumber) {
        return ResponseEntity.ok(headerService.getHeaderByAccountNumber(accountNumber));
    }

    @GetMapping("/facility-type/{typeOfFacility}")
    public ResponseEntity<List<PreShipmentTurnoverHeaderDTO>> getHeadersByTypeOfFacility(@PathVariable String typeOfFacility) {
        return ResponseEntity.ok(headerService.getHeadersByTypeOfFacility(typeOfFacility));
    }

    @GetMapping("/industry-type/{industryType}")
    public ResponseEntity<List<PreShipmentTurnoverHeaderDTO>> getHeadersByIndustryType(@PathVariable String industryType) {
        return ResponseEntity.ok(headerService.getHeadersByIndustryType(industryType));
    }

    @GetMapping("/date-approved-range")
    public ResponseEntity<List<PreShipmentTurnoverHeaderDTO>> getHeadersByDateApprovedRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(headerService.getHeadersByDateApprovedRange(startDate, endDate));
    }

    @GetMapping("/reporting-date/{reportingDate}")
    public ResponseEntity<List<PreShipmentTurnoverHeaderDTO>> getHeadersByReportingDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportingDate) {
        return ResponseEntity.ok(headerService.getHeadersByReportingDate(reportingDate));
    }

    // Turnover record management endpoints
    @PostMapping("/{headerId}/turnovers")
    public ResponseEntity<PreShipmentTurnoverHeaderDTO> addTurnoverRecord(
            @PathVariable Long headerId,
            @RequestBody PreShipmentTurnoverDTO turnoverDTO) {
        return new ResponseEntity<>(headerService.addTurnoverRecord(headerId, turnoverDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{headerId}/turnovers/{turnoverId}")
    public ResponseEntity<PreShipmentTurnoverHeaderDTO> updateTurnoverRecord(
            @PathVariable Long headerId,
            @PathVariable Long turnoverId,
             @RequestBody PreShipmentTurnoverDTO turnoverDTO) {
        return ResponseEntity.ok(headerService.updateTurnoverRecord(headerId, turnoverId, turnoverDTO));
    }

    @DeleteMapping("/{headerId}/turnovers/{turnoverId}")
    public ResponseEntity<PreShipmentTurnoverHeaderDTO> removeTurnoverRecord(
            @PathVariable Long headerId,
            @PathVariable Long turnoverId) {
        return ResponseEntity.ok(headerService.removeTurnoverRecord(headerId, turnoverId));
    }

    // Statistics endpoints
    @GetMapping("/{headerId}/statistics")
    public ResponseEntity<Map<String, Object>> getHeaderStatistics(@PathVariable Long headerId) {
        return ResponseEntity.ok(headerService.getHeaderStatistics(headerId));
    }

    @GetMapping("/{headerId}/totals/debit")
    public ResponseEntity<BigDecimal> getTotalDebit(@PathVariable Long headerId) {
        return ResponseEntity.ok(headerService.calculateTotalDebit(headerId));
    }

    @GetMapping("/{headerId}/totals/credit")
    public ResponseEntity<BigDecimal> getTotalCredit(@PathVariable Long headerId) {
        return ResponseEntity.ok(headerService.calculateTotalCredit(headerId));
    }

    @GetMapping("/{headerId}/totals/net")
    public ResponseEntity<BigDecimal> getNetTurnover(@PathVariable Long headerId) {
        return ResponseEntity.ok(headerService.calculateNetTurnover(headerId));
    }

    @GetMapping("/{headerId}/monthly-statistics")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyStatistics(@PathVariable Long headerId) {
        return ResponseEntity.ok(headerService.getMonthlyStatistics(headerId));
    }
}