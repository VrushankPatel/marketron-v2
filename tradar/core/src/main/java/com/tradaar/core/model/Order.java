package com.tradar.core.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class Order implements Serializable {
    private final String orderId;
    private final String symbol;
    private final OrderSide side;
    private final OrderType type;
    private final BigDecimal quantity;
    private final BigDecimal price;
    private final TimeInForce timeInForce;
    private final LocalDateTime timestamp;
    private OrderStatus status;

    public static Order createNewOrder(String symbol, OrderSide side, OrderType type, 
                                     BigDecimal quantity, BigDecimal price, TimeInForce timeInForce) {
        return Order.builder()
                .orderId(UUID.randomUUID().toString())
                .symbol(symbol)
                .side(side)
                .type(type)
                .quantity(quantity)
                .price(price)
                .timeInForce(timeInForce)
                .timestamp(LocalDateTime.now())
                .status(OrderStatus.NEW)
                .build();
    }
} 