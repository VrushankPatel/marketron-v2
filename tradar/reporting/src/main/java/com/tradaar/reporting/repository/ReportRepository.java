package com.tradar.reporting.repository;

import com.tradar.core.model.ExecutionReport;
import com.tradar.core.model.TradeCaptureReport;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Repository for storing and retrieving execution reports and trade capture reports.
 * Provides methods to query reports by various criteria and time ranges.
 * Implements thread-safe storage using concurrent collections.
 *
 * @author Vrushank Patel
 */
@Slf4j
public class ReportRepository {
    private final ConcurrentHashMap<String, ExecutionReport> executionReports;
    private final ConcurrentHashMap<String, TradeCaptureReport> tradeReports;

    public ReportRepository() {
        this.executionReports = new ConcurrentHashMap<>();
        this.tradeReports = new ConcurrentHashMap<>();
    }

    public void addExecutionReport(ExecutionReport report) {
        executionReports.put(report.getExecId(), report);
        log.info("Added execution report: {}", report);
    }

    public void addTradeCaptureReport(TradeCaptureReport report) {
        tradeReports.put(report.getTradeReportId(), report);
        log.info("Added trade capture report: {}", report);
    }

    public List<ExecutionReport> getExecutionReports(LocalDateTime from, LocalDateTime to) {
        return executionReports.values().stream()
                .filter(r -> !r.getTransactTime().isBefore(from) && !r.getTransactTime().isAfter(to))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<TradeCaptureReport> getTradeReports(LocalDateTime from, LocalDateTime to) {
        return tradeReports.values().stream()
                .filter(r -> !r.getTransactTime().isBefore(from) && !r.getTransactTime().isAfter(to))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<ExecutionReport> getExecutionReportsByOrderId(String orderId) {
        return executionReports.values().stream()
                .filter(r -> r.getOrderId().equals(orderId))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<TradeCaptureReport> getTradeReportsBySymbol(String symbol) {
        return tradeReports.values().stream()
                .filter(r -> r.getSymbol().equals(symbol))
                .collect(Collectors.toCollection(ArrayList::new));
    }
} 