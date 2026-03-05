package com.cbo.credit_scoring.controllers;

import com.cbo.credit_scoring.dtos.AccountStatementDTO;
import com.cbo.credit_scoring.services.AccountStatementService;
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
@RequestMapping("/api/v1/account-statement/statements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AccountStatementController {

    private final AccountStatementService statementService;

    // ============= Statement CRUD Operations =============

    @PostMapping
    public ResponseEntity<AccountStatementDTO> createStatement(
             @RequestBody AccountStatementDTO statementDTO) {
        return new ResponseEntity<>(statementService.createStatement(statementDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountStatementDTO> updateStatement(
            @PathVariable Long id,
             @RequestBody AccountStatementDTO statementDTO) {
        return ResponseEntity.ok(statementService.updateStatement(id, statementDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountStatementDTO> getStatementById(@PathVariable Long id) {
        return ResponseEntity.ok(statementService.getStatementById(id));
    }

    @GetMapping
    public ResponseEntity<Page<AccountStatementDTO>> getAllStatements(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(statementService.getAllStatements(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStatement(@PathVariable Long id) {
        statementService.deleteStatement(id);
        return ResponseEntity.noContent().build();
    }

    // ============= Search Operations =============

    @GetMapping("/by-header/{headerId}")
    public ResponseEntity<List<AccountStatementDTO>> getStatementsByHeaderId(@PathVariable Long headerId) {
        return ResponseEntity.ok(statementService.getStatementsByHeaderId(headerId));
    }

    @GetMapping("/by-header/{headerId}/ordered")
    public ResponseEntity<List<AccountStatementDTO>> getStatementsByHeaderIdOrdered(
            @PathVariable Long headerId,
            @RequestParam(defaultValue = "true") boolean ascending) {
        return ResponseEntity.ok(statementService.getStatementsByHeaderIdOrdered(headerId, ascending));
    }

    @GetMapping("/by-month")
    public ResponseEntity<List<AccountStatementDTO>> getStatementsByMonth(@RequestParam String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        return ResponseEntity.ok(statementService.getStatementsByMonth(yearMonth));
    }

    @GetMapping("/by-month-range")
    public ResponseEntity<List<AccountStatementDTO>> getStatementsByMonthRange(
            @RequestParam String startMonth,
            @RequestParam String endMonth) {
        YearMonth start = YearMonth.parse(startMonth);
        YearMonth end = YearMonth.parse(endMonth);
        return ResponseEntity.ok(statementService.getStatementsByMonthRange(start, end));
    }

    @GetMapping("/by-header/{headerId}/month/{month}")
    public ResponseEntity<AccountStatementDTO> getStatementByHeaderIdAndMonth(
            @PathVariable Long headerId,
            @PathVariable String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        return ResponseEntity.ok(statementService.getStatementByHeaderIdAndMonth(headerId, yearMonth));
    }

    // ============= Trends and Analytics =============

    @GetMapping("/trends/credit/{headerId}")
    public ResponseEntity<Map<YearMonth, BigDecimal>> getMonthlyCreditTrend(@PathVariable Long headerId) {
        return ResponseEntity.ok(statementService.getMonthlyCreditTrend(headerId));
    }

    @GetMapping("/trends/debit/{headerId}")
    public ResponseEntity<Map<YearMonth, BigDecimal>> getMonthlyDebitTrend(@PathVariable Long headerId) {
        return ResponseEntity.ok(statementService.getMonthlyDebitTrend(headerId));
    }

    @GetMapping("/trends/utilization/{headerId}")
    public ResponseEntity<Map<YearMonth, BigDecimal>> getMonthlyUtilizationTrend(@PathVariable Long headerId) {
        return ResponseEntity.ok(statementService.getMonthlyUtilizationTrend(headerId));
    }

    @GetMapping("/trends/net/{headerId}")
    public ResponseEntity<Map<YearMonth, BigDecimal>> getMonthlyNetTurnoverTrend(@PathVariable Long headerId) {
        return ResponseEntity.ok(statementService.getMonthlyNetTurnoverTrend(headerId));
    }

    // ============= Special Queries =============

    @GetMapping("/highest-utilization/{headerId}")
    public ResponseEntity<AccountStatementDTO> getMonthWithHighestUtilization(@PathVariable Long headerId) {
        return ResponseEntity.ok(statementService.getMonthWithHighestUtilization(headerId));
    }

    @GetMapping("/lowest-utilization/{headerId}")
    public ResponseEntity<AccountStatementDTO> getMonthWithLowestUtilization(@PathVariable Long headerId) {
        return ResponseEntity.ok(statementService.getMonthWithLowestUtilization(headerId));
    }

    @GetMapping("/highest-credit/{headerId}")
    public ResponseEntity<AccountStatementDTO> getMonthWithHighestCredit(@PathVariable Long headerId) {
        return ResponseEntity.ok(statementService.getMonthWithHighestCredit(headerId));
    }

    @GetMapping("/highest-debit/{headerId}")
    public ResponseEntity<AccountStatementDTO> getMonthWithHighestDebit(@PathVariable Long headerId) {
        return ResponseEntity.ok(statementService.getMonthWithHighestDebit(headerId));
    }

    @GetMapping("/latest/{headerId}")
    public ResponseEntity<AccountStatementDTO> getLatestStatement(@PathVariable Long headerId) {
        return ResponseEntity.ok(statementService.getLatestStatement(headerId));
    }

    @GetMapping("/oldest/{headerId}")
    public ResponseEntity<AccountStatementDTO> getOldestStatement(@PathVariable Long headerId) {
        return ResponseEntity.ok(statementService.getOldestStatement(headerId));
    }

    @GetMapping("/high-utilization/{headerId}")
    public ResponseEntity<List<AccountStatementDTO>> getHighUtilizationMonths(
            @PathVariable Long headerId,
            @RequestParam(defaultValue = "80") BigDecimal threshold) {
        return ResponseEntity.ok(statementService.getHighUtilizationMonths(headerId, threshold));
    }

    @GetMapping("/low-utilization/{headerId}")
    public ResponseEntity<List<AccountStatementDTO>> getLowUtilizationMonths(
            @PathVariable Long headerId,
            @RequestParam(defaultValue = "30") BigDecimal threshold) {
        return ResponseEntity.ok(statementService.getLowUtilizationMonths(headerId, threshold));
    }

    // ============= Summary and Analysis =============

    @GetMapping("/summary/{headerId}")
    public ResponseEntity<Map<String, Object>> getStatementSummary(@PathVariable Long headerId) {
        return ResponseEntity.ok(statementService.getStatementSummary(headerId));
    }

    @GetMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareHeaders(
            @RequestParam Long headerId1,
            @RequestParam Long headerId2) {
        return ResponseEntity.ok(statementService.getComparativeAnalysis(headerId1, headerId2));
    }

    // ============= Batch Operations =============

    @PostMapping("/batch/{headerId}")
    public ResponseEntity<List<AccountStatementDTO>> createBatchStatements(
            @PathVariable Long headerId,
             @RequestBody List<AccountStatementDTO> statementDTOs) {
        List<AccountStatementDTO> createdStatements = statementDTOs.stream()
                .map(dto -> {
                    dto.setHeaderId(headerId);
                    return statementService.createStatement(dto);
                })
                .toList();
        return new ResponseEntity<>(createdStatements, HttpStatus.CREATED);
    }

    @DeleteMapping("/batch/by-header/{headerId}")
    public ResponseEntity<Void> deleteAllStatementsByHeader(@PathVariable Long headerId) {
        List<AccountStatementDTO> statements = statementService.getStatementsByHeaderId(headerId);
        statements.forEach(s -> statementService.deleteStatement(s.getId()));
        return ResponseEntity.noContent().build();
    }
}