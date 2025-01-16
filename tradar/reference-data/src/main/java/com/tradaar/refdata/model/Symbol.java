package com.tradar.refdata.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Symbol {
    private final String symbol;
    private final String description;
    private final String exchange;
    private final String currency;
    private final BigDecimal tickSize;
    private final BigDecimal lotSize;
    private final BigDecimal minQty;
    private final BigDecimal maxQty;
    private final BigDecimal maxPriceDeviation;
    private final boolean active;
} 