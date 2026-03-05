package com.cbo.credit_scoring.controllers;

import com.cbo.credit_scoring.dtos.OdTurnoverDTO;
import com.cbo.credit_scoring.services.OdTurnoverService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/od/turnovers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OdTurnoverController {

    private final OdTurnoverService turnoverService;

    @PostMapping
    public ResponseEntity<OdTurnoverDTO> createTurnover( @RequestBody OdTurnoverDTO turnoverDTO) {
        return new ResponseEntity<>(turnoverService.createTurnover(turnoverDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OdTurnoverDTO> updateTurnover(
            @PathVariable Long id,
             @RequestBody OdTurnoverDTO turnoverDTO) {
        return ResponseEntity.ok(turnoverService.updateTurnover(id, turnoverDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OdTurnoverDTO> getTurnoverById(@PathVariable Long id) {
        return ResponseEntity.ok(turnoverService.getTurnoverById(id));
    }

    @GetMapping
    public ResponseEntity<Page<OdTurnoverDTO>> getAllTurnovers(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(turnoverService.getAllTurnovers(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTurnover(@PathVariable Long id) {
        turnoverService.deleteTurnover(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-header/{headerId}")
    public ResponseEntity<List<OdTurnoverDTO>> getTurnoversByHeaderId(@PathVariable Long headerId) {
        return ResponseEntity.ok(turnoverService.getTurnoversByHeaderId(headerId));
    }

    @GetMapping("/by-month")
    public ResponseEntity<List<OdTurnoverDTO>> getTurnoversByMonth(@RequestParam String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        return ResponseEntity.ok(turnoverService.getTurnoversByMonth(yearMonth));
    }

    @GetMapping("/by-month-range")
    public ResponseEntity<List<OdTurnoverDTO>> getTurnoversByMonthRange(
            @RequestParam String startMonth,
            @RequestParam String endMonth) {
        YearMonth start = YearMonth.parse(startMonth);
        YearMonth end = YearMonth.parse(endMonth);
        return ResponseEntity.ok(turnoverService.getTurnoversByMonthRange(start, end));
    }

    @GetMapping("/by-header/{headerId}/month/{month}")
    public ResponseEntity<OdTurnoverDTO> getTurnoverByHeaderIdAndMonth(
            @PathVariable Long headerId,
            @PathVariable String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        return ResponseEntity.ok(turnoverService.getTurnoverByHeaderIdAndMonth(headerId, yearMonth));
    }

    @GetMapping("/trends/credit/{headerId}")
    public ResponseEntity<Map<YearMonth, BigDecimal>> getMonthlyCreditTrend(@PathVariable Long headerId) {
        return ResponseEntity.ok(turnoverService.getMonthlyCreditTrend(headerId));
    }

    @GetMapping("/trends/debit/{headerId}")
    public ResponseEntity<Map<YearMonth, BigDecimal>> getMonthlyDebitTrend(@PathVariable Long headerId) {
        return ResponseEntity.ok(turnoverService.getMonthlyDebitTrend(headerId));
    }

    @GetMapping("/trends/utilization/{headerId}")
    public ResponseEntity<Map<YearMonth, BigDecimal>> getMonthlyUtilizationTrend(@PathVariable Long headerId) {
        return ResponseEntity.ok(turnoverService.getMonthlyUtilizationTrend(headerId));
    }

    @GetMapping("/high-utilization/{headerId}")
    public ResponseEntity<List<OdTurnoverDTO>> getHighUtilizationMonths(
            @PathVariable Long headerId,
            @RequestParam(defaultValue = "80") BigDecimal threshold) {
        return ResponseEntity.ok(turnoverService.getHighUtilizationMonths(headerId, threshold));
    }

    @GetMapping("/summary/{headerId}")
    public ResponseEntity<Map<String, Object>> getTurnoverSummary(@PathVariable Long headerId) {
        return ResponseEntity.ok(turnoverService.getTurnoverSummary(headerId));
    }
}