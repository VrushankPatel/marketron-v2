package com.tradar.core.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ExecutionReport {
    private final String execId;
    private final String orderId;
    private final String symbol;
    private final OrderSide side;
    private final OrderType orderType;
    private final BigDecimal orderQty;
    private final BigDecimal leavesQty;
    private final BigDecimal cumQty;
    private final BigDecimal avgPx;
    private final OrderStatus orderStatus;
    private final LocalDateTime transactTime;
    private final String text;

    public static ExecutionReport fromOrder(Order order, String execId) {
        return ExecutionReport.builder()
                .execId(execId)
                .orderId(order.getOrderId())
                .symbol(order.getSymbol())
                .side(order.getSide())
                .orderType(order.getType())
                .orderQty(order.getQuantity())
                .leavesQty(order.getQuantity()) // Initial leaves qty is full order qty
                .cumQty(BigDecimal.ZERO)
                .avgPx(BigDecimal.ZERO)
                .orderStatus(order.getStatus())
                .transactTime(LocalDateTime.now())
                .text("New order")
                .build();
    }
} 