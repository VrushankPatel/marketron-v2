package com.tradar.marketdata.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MarketDataEntry {
    private final String symbol;
    private final BigDecimal bidPrice;
    private final BigDecimal askPrice;
    private final BigDecimal bidSize;
    private final BigDecimal askSize;
    private final LocalDateTime timestamp;
    private final BigDecimal lastPrice;
    private final BigDecimal lastSize;
    private final BigDecimal volume;
    private final BigDecimal high;
    private final BigDecimal low;
} 