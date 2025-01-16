package com.tradar.core.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TradeCaptureReport {
    private final String tradeReportId;
    private final String tradeId;
    private final String symbol;
    private final String buyOrderId;
    private final String sellOrderId;
    private final BigDecimal price;
    private final BigDecimal quantity;
    private final LocalDateTime tradeDate;
    private final LocalDateTime transactTime;

    public static TradeCaptureReport fromTrade(Trade trade) {
        return TradeCaptureReport.builder()
                .tradeReportId(trade.getTradeId() + "-TCR")
                .tradeId(trade.getTradeId())
                .symbol(trade.getSymbol())
                .buyOrderId(trade.getBuyOrderId())
                .sellOrderId(trade.getSellOrderId())
                .price(trade.getPrice())
                .quantity(trade.getQuantity())
                .tradeDate(trade.getTimestamp().toLocalDate().atStartOfDay())
                .transactTime(trade.getTimestamp())
                .build();
    }
} 