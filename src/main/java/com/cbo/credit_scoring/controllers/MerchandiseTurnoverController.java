package com.cbo.credit_scoring.controllers;

import com.cbo.credit_scoring.dtos.MerchandiseTurnoverDTO;
import com.cbo.credit_scoring.services.MerchandiseTurnoverService;
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
import java.time.YearMonth;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/merchandise/turnovers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MerchandiseTurnoverController {

    private final MerchandiseTurnoverService turnoverService;

    // ============= Turnover CRUD Operations =============

    @PostMapping
    public ResponseEntity<MerchandiseTurnoverDTO> createTurnover(
             @RequestBody MerchandiseTurnoverDTO turnoverDTO) {
        return new ResponseEntity<>(turnoverService.createTurnover(turnoverDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MerchandiseTurnoverDTO> updateTurnover(
            @PathVariable Long id,
             @RequestBody MerchandiseTurnoverDTO turnoverDTO) {
        return ResponseEntity.ok(turnoverService.updateTurnover(id, turnoverDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MerchandiseTurnoverDTO> getTurnoverById(@PathVariable Long id) {
        return ResponseEntity.ok(turnoverService.getTurnoverById(id));
    }

    @GetMapping
    public ResponseEntity<Page<MerchandiseTurnoverDTO>> getAllTurnovers(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(turnoverService.getAllTurnovers(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTurnover(@PathVariable Long id) {
        turnoverService.deleteTurnover(id);
        return ResponseEntity.noContent().build();
    }

    // ============= Search Operations =============

    @GetMapping("/by-header/{headerId}")
    public ResponseEntity<List<MerchandiseTurnoverDTO>> getTurnoversByHeaderId(@PathVariable Long headerId) {
        return ResponseEntity.ok(turnoverService.getTurnoversByHeaderId(headerId));
    }

    @GetMapping("/by-month")
    public ResponseEntity<List<MerchandiseTurnoverDTO>> getTurnoversByMonth(@RequestParam String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        return ResponseEntity.ok(turnoverService.getTurnoversByMonth(yearMonth));
    }

    @GetMapping("/by-month-range")
    public ResponseEntity<List<MerchandiseTurnoverDTO>> getTurnoversByDateRange(
            @RequestParam String startMonth,
            @RequestParam String endMonth) {
        YearMonth start = YearMonth.parse(startMonth);
        YearMonth end = YearMonth.parse(endMonth);
        return ResponseEntity.ok(turnoverService.getTurnoversByDateRange(start, end));
    }

    @GetMapping("/by-header/{headerId}/month/{month}")
    public ResponseEntity<MerchandiseTurnoverDTO> getTurnoverByHeaderIdAndMonth(
            @PathVariable Long headerId,
            @PathVariable String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        // Note: You might want to add this specific method to your service
        List<MerchandiseTurnoverDTO> turnovers = turnoverService.getTurnoversByHeaderId(headerId);
        return turnovers.stream()
                .filter(t -> t.getMonth().equals(yearMonth))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ============= Aggregation Operations =============

    @GetMapping("/calculate/debit/{headerId}")
    public ResponseEntity<Map<String, Object>> calculateTotalDebitByHeader(@PathVariable Long headerId) {
        BigDecimal totalDebit = turnoverService.calculateTotalDebitByHeader(headerId);
        Map<String, Object> response = new HashMap<>();
        response.put("headerId", headerId);
        response.put("totalDebit", totalDebit);
        response.put("calculation", "Total Debit Disbursements");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/calculate/credit/{headerId}")
    public ResponseEntity<Map<String, Object>> calculateTotalCreditByHeader(@PathVariable Long headerId) {
        BigDecimal totalCredit = turnoverService.calculateTotalCreditByHeader(headerId);
        Map<String, Object> response = new HashMap<>();
        response.put("headerId", headerId);
        response.put("totalCredit", totalCredit);
        response.put("calculation", "Total Credit Principal Repayments");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/calculate/net/{headerId}")
    public ResponseEntity<Map<String, Object>> calculateNetTurnoverByHeader(@PathVariable Long headerId) {
        BigDecimal netTurnover = turnoverService.calculateNetTurnoverByHeader(headerId);
        Map<String, Object> response = new HashMap<>();
        response.put("headerId", headerId);
        response.put("netTurnover", netTurnover);
        response.put("calculation", "Net Turnover (Debit - Credit)");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/calculate/summary/{headerId}")
    public ResponseEntity<Map<String, Object>> getFullSummaryByHeader(@PathVariable Long headerId) {
        BigDecimal totalDebit = turnoverService.calculateTotalDebitByHeader(headerId);
        BigDecimal totalCredit = turnoverService.calculateTotalCreditByHeader(headerId);
        BigDecimal netTurnover = turnoverService.calculateNetTurnoverByHeader(headerId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("headerId", headerId);
        summary.put("totalDebit", totalDebit);
        summary.put("totalCredit", totalCredit);
        summary.put("netTurnover", netTurnover);
        summary.put("totalTransactions", turnoverService.getTurnoversByHeaderId(headerId).size());

        return ResponseEntity.ok(summary);
    }

    // ============= Batch Operations =============

    @PostMapping("/batch/{headerId}")
    public ResponseEntity<List<MerchandiseTurnoverDTO>> createBatchTurnovers(
            @PathVariable Long headerId,
             @RequestBody List<MerchandiseTurnoverDTO> turnoverDTOs) {
        // Note: You might want to add this method to your service
        List<MerchandiseTurnoverDTO> createdTurnovers = turnoverDTOs.stream()
                .map(dto -> {
                    dto.setHeaderId(headerId);
                    return turnoverService.createTurnover(dto);
                })
                .toList();
        return new ResponseEntity<>(createdTurnovers, HttpStatus.CREATED);
    }

    @DeleteMapping("/batch/by-header/{headerId}")
    public ResponseEntity<Void> deleteAllTurnoversByHeader(@PathVariable Long headerId) {
        // Note: You might want to add this method to your service
        List<MerchandiseTurnoverDTO> turnovers = turnoverService.getTurnoversByHeaderId(headerId);
        turnovers.forEach(t -> turnoverService.deleteTurnover(t.getId()));
        return ResponseEntity.noContent().build();
    }

    // ============= Statistics Endpoints =============

    @GetMapping("/statistics/monthly/{headerId}")
    public ResponseEntity<Map<YearMonth, Map<String, BigDecimal>>> getMonthlyStatistics(
            @PathVariable Long headerId) {
        List<MerchandiseTurnoverDTO> turnovers = turnoverService.getTurnoversByHeaderId(headerId);

        Map<YearMonth, Map<String, BigDecimal>> monthlyStats = new HashMap<>();

        for (MerchandiseTurnoverDTO turnover : turnovers) {
            YearMonth month = turnover.getMonth();
            Map<String, BigDecimal> stats = monthlyStats.getOrDefault(month, new HashMap<>());

            stats.put("debit", stats.getOrDefault("debit", BigDecimal.ZERO)
                    .add(turnover.getDebitDisbursements() != null ? turnover.getDebitDisbursements() : BigDecimal.ZERO));
            stats.put("credit", stats.getOrDefault("credit", BigDecimal.ZERO)
                    .add(turnover.getCreditPrincipalRepayments() != null ? turnover.getCreditPrincipalRepayments() : BigDecimal.ZERO));

            monthlyStats.put(month, stats);
        }

        // Calculate net for each month
        monthlyStats.forEach((month, stats) -> {
            BigDecimal net = stats.getOrDefault("debit", BigDecimal.ZERO)
                    .subtract(stats.getOrDefault("credit", BigDecimal.ZERO));
            stats.put("net", net);
        });

        return ResponseEntity.ok(monthlyStats);
    }

    @GetMapping("/statistics/years/{headerId}")
    public ResponseEntity<Map<Integer, Map<String, Object>>> getYearlyStatistics(
            @PathVariable Long headerId) {
        List<MerchandiseTurnoverDTO> turnovers = turnoverService.getTurnoversByHeaderId(headerId);

        Map<Integer, Map<String, Object>> yearlyStats = new HashMap<>();

        for (MerchandiseTurnoverDTO turnover : turnovers) {
            if (turnover.getMonth() == null) continue;

            Integer year = turnover.getMonth().getYear();
            Map<String, Object> stats = yearlyStats.getOrDefault(year, new HashMap<>());

            BigDecimal yearDebit = (BigDecimal) stats.getOrDefault("totalDebit", BigDecimal.ZERO);
            BigDecimal yearCredit = (BigDecimal) stats.getOrDefault("totalCredit", BigDecimal.ZERO);

            stats.put("totalDebit", yearDebit.add(turnover.getDebitDisbursements() != null ?
                    turnover.getDebitDisbursements() : BigDecimal.ZERO));
            stats.put("totalCredit", yearCredit.add(turnover.getCreditPrincipalRepayments() != null ?
                    turnover.getCreditPrincipalRepayments() : BigDecimal.ZERO));

            List<YearMonth> months = (List<YearMonth>) stats.getOrDefault("months", new java.util.ArrayList<>());
            months.add(turnover.getMonth());
            stats.put("months", months);
            stats.put("monthCount", months.size());

            yearlyStats.put(year, stats);
        }

        // Calculate net for each year
        yearlyStats.forEach((year, stats) -> {
            BigDecimal totalDebit = (BigDecimal) stats.get("totalDebit");
            BigDecimal totalCredit = (BigDecimal) stats.get("totalCredit");
            stats.put("netTurnover", totalDebit.subtract(totalCredit));
        });

        return ResponseEntity.ok(yearlyStats);
    }
}