package com.cbo.credit_scoring.controllers;

import com.cbo.credit_scoring.dtos.MerchandiseTurnoverHeaderDTO;
import com.cbo.credit_scoring.dtos.MerchandiseTurnoverDTO;
import com.cbo.credit_scoring.services.MerchandiseTurnoverHeaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/merchandise/headers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MerchandiseTurnoverHeaderController {

    private final MerchandiseTurnoverHeaderService headerService;

    // ============= Header CRUD Operations =============

    @PostMapping
    public ResponseEntity<MerchandiseTurnoverHeaderDTO> createHeader(
             @RequestBody MerchandiseTurnoverHeaderDTO headerDTO) {
        return new ResponseEntity<>(headerService.createHeader(headerDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MerchandiseTurnoverHeaderDTO> updateHeader(
            @PathVariable Long id,
             @RequestBody MerchandiseTurnoverHeaderDTO headerDTO) {
        return ResponseEntity.ok(headerService.updateHeader(id, headerDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MerchandiseTurnoverHeaderDTO> getHeaderById(@PathVariable Long id) {
        return ResponseEntity.ok(headerService.getHeaderById(id));
    }

    @GetMapping
    public ResponseEntity<Page<MerchandiseTurnoverHeaderDTO>> getAllHeaders(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(headerService.getAllHeaders(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHeader(@PathVariable Long id) {
        headerService.deleteHeader(id);
        return ResponseEntity.noContent().build();
    }

    // ============= Search Operations =============

    @GetMapping("/search/by-customer")
    public ResponseEntity<List<MerchandiseTurnoverHeaderDTO>> searchByCustomerName(
            @RequestParam String customerName) {
        return ResponseEntity.ok(headerService.searchByCustomerName(customerName));
    }

    @GetMapping("/search/by-account")
    public ResponseEntity<List<MerchandiseTurnoverHeaderDTO>> searchByAccountNumber(
            @RequestParam String accountNumber) {
        return ResponseEntity.ok(headerService.searchByAccountNumber(accountNumber));
    }

    @GetMapping("/search/by-date-approved-range")
    public ResponseEntity<List<MerchandiseTurnoverHeaderDTO>> searchByDateApprovedBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(headerService.searchByDateApprovedBetween(startDate, endDate));
    }

    @GetMapping("/search/by-reporting-date")
    public ResponseEntity<List<MerchandiseTurnoverHeaderDTO>> searchByReportingDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportingDate) {
        return ResponseEntity.ok(headerService.searchByReportingDate(reportingDate));
    }

    // ============= Turnover Record Management =============

    @PostMapping("/{headerId}/turnovers")
    public ResponseEntity<MerchandiseTurnoverHeaderDTO> addTurnoverRecord(
            @PathVariable Long headerId,
             @RequestBody MerchandiseTurnoverDTO turnoverDTO) {
        return new ResponseEntity<>(headerService.addTurnoverRecord(headerId, turnoverDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{headerId}/turnovers/{turnoverId}")
    public ResponseEntity<MerchandiseTurnoverHeaderDTO> updateTurnoverRecord(
            @PathVariable Long headerId,
            @PathVariable Long turnoverId,
             @RequestBody MerchandiseTurnoverDTO turnoverDTO) {
        return ResponseEntity.ok(headerService.updateTurnoverRecord(headerId, turnoverId, turnoverDTO));
    }

    @DeleteMapping("/{headerId}/turnovers/{turnoverId}")
    public ResponseEntity<MerchandiseTurnoverHeaderDTO> removeTurnoverRecord(
            @PathVariable Long headerId,
            @PathVariable Long turnoverId) {
        return ResponseEntity.ok(headerService.removeTurnoverRecord(headerId, turnoverId));
    }

    // ============= Additional Utility Endpoints =============

    @GetMapping("/exists/account/{accountNumber}")
    public ResponseEntity<Boolean> checkAccountNumberExists(@PathVariable String accountNumber) {
        // Note: You might want to add this method to your service
        List<MerchandiseTurnoverHeaderDTO> results = headerService.searchByAccountNumber(accountNumber);
        return ResponseEntity.ok(!results.isEmpty());
    }

    @GetMapping("/count/by-date-approved-range")
    public ResponseEntity<Integer> countByDateApprovedRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<MerchandiseTurnoverHeaderDTO> results = headerService.searchByDateApprovedBetween(startDate, endDate);
        return ResponseEntity.ok(results.size());
    }
}