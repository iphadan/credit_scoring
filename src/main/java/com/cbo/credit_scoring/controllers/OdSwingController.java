package com.cbo.credit_scoring.controllers;

import com.cbo.credit_scoring.dtos.OdSwingDTO;
import com.cbo.credit_scoring.services.OdSwingService;
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
@RequestMapping("/api/v1/od-swing/swings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OdSwingController {

    private final OdSwingService swingService;

    @PostMapping
    public ResponseEntity<OdSwingDTO> createSwing( @RequestBody OdSwingDTO swingDTO) {
        return new ResponseEntity<>(swingService.createSwing(swingDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OdSwingDTO> updateSwing(
            @PathVariable Long id,
             @RequestBody OdSwingDTO swingDTO) {
        return ResponseEntity.ok(swingService.updateSwing(id, swingDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OdSwingDTO> getSwingById(@PathVariable Long id) {
        return ResponseEntity.ok(swingService.getSwingById(id));
    }

    @GetMapping
    public ResponseEntity<Page<OdSwingDTO>> getAllSwings(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(swingService.getAllSwings(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSwing(@PathVariable Long id) {
        swingService.deleteSwing(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-header/{headerId}")
    public ResponseEntity<List<OdSwingDTO>> getSwingsByHeaderId(@PathVariable Long headerId) {
        return ResponseEntity.ok(swingService.getSwingsByHeaderId(headerId));
    }

    @GetMapping("/by-month")
    public ResponseEntity<List<OdSwingDTO>> getSwingsByMonth(@RequestParam String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        return ResponseEntity.ok(swingService.getSwingsByMonth(yearMonth));
    }

    @GetMapping("/by-month-range")
    public ResponseEntity<List<OdSwingDTO>> getSwingsByMonthRange(
            @RequestParam String startMonth,
            @RequestParam String endMonth) {
        YearMonth start = YearMonth.parse(startMonth);
        YearMonth end = YearMonth.parse(endMonth);
        return ResponseEntity.ok(swingService.getSwingsByMonthRange(start, end));
    }

    @GetMapping("/by-header/{headerId}/month/{month}")
    public ResponseEntity<OdSwingDTO> getSwingByHeaderIdAndMonth(
            @PathVariable Long headerId,
            @PathVariable String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        return ResponseEntity.ok(swingService.getSwingByHeaderIdAndMonth(headerId, yearMonth));
    }

    @GetMapping("/by-date-high-range")
    public ResponseEntity<List<OdSwingDTO>> getSwingsByDateHighRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(swingService.getSwingsByDateHighRange(startDate, endDate));
    }

    @GetMapping("/by-date-low-range")
    public ResponseEntity<List<OdSwingDTO>> getSwingsByDateLowRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(swingService.getSwingsByDateLowRange(startDate, endDate));
    }

    @GetMapping("/trends/utilization/{headerId}")
    public ResponseEntity<Map<YearMonth, BigDecimal>> getMonthlyUtilizationTrend(@PathVariable Long headerId) {
        return ResponseEntity.ok(swingService.getMonthlyUtilizationTrend(headerId));
    }

    @GetMapping("/trends/swing-range/{headerId}")
    public ResponseEntity<Map<YearMonth, BigDecimal>> getMonthlySwingRangeTrend(@PathVariable Long headerId) {
        return ResponseEntity.ok(swingService.getMonthlySwingRangeTrend(headerId));
    }

    @GetMapping("/highest-utilization/{headerId}")
    public ResponseEntity<OdSwingDTO> getMonthWithHighestUtilization(@PathVariable Long headerId) {
        return ResponseEntity.ok(swingService.getMonthWithHighestUtilization(headerId));
    }

    @GetMapping("/lowest-utilization/{headerId}")
    public ResponseEntity<OdSwingDTO> getMonthWithLowestUtilization(@PathVariable Long headerId) {
        return ResponseEntity.ok(swingService.getMonthWithLowestUtilization(headerId));
    }

    @GetMapping("/widest-swing/{headerId}")
    public ResponseEntity<OdSwingDTO> getMonthWithWidestSwing(@PathVariable Long headerId) {
        return ResponseEntity.ok(swingService.getMonthWithWidestSwing(headerId));
    }

    @GetMapping("/high-utilization/{headerId}")
    public ResponseEntity<List<OdSwingDTO>> getHighUtilizationMonths(
            @PathVariable Long headerId,
            @RequestParam(defaultValue = "80") BigDecimal threshold) {
        return ResponseEntity.ok(swingService.getHighUtilizationMonths(headerId, threshold));
    }

    @GetMapping("/summary/{headerId}")
    public ResponseEntity<Map<String, Object>> getSwingSummary(@PathVariable Long headerId) {
        return ResponseEntity.ok(swingService.getSwingSummary(headerId));
    }
}