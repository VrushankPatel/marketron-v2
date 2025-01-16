package com.tradar.core.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class Trade {
    private final String tradeId;
    private final String buyOrderId;
    private final String sellOrderId;
    private final String symbol;
    private final BigDecimal price;
    private final BigDecimal quantity;
    private final LocalDateTime timestamp;

    public static Trade createTrade(Order buyOrder, Order sellOrder, BigDecimal matchedQuantity) {
        return Trade.builder()
                .tradeId(UUID.randomUUID().toString())
                .buyOrderId(buyOrder.getOrderId())
                .sellOrderId(sellOrder.getOrderId())
                .symbol(buyOrder.getSymbol())
                .price(sellOrder.getPrice())
                .quantity(matchedQuantity)
                .timestamp(LocalDateTime.now())
                .build();
    }
} 