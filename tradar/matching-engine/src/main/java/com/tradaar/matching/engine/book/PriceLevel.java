package com.tradar.matching.engine.book;

import com.tradar.core.model.Order;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a price level in the order book, maintaining orders at a specific price.
 * Implements time priority through ordered storage of orders and tracks total quantity
 * at the price level.
 *
 * @author Vrushank Patel
 */
public class PriceLevel {
    @Getter
    private final BigDecimal price;
    private final Map<String, Order> orders;
    @Getter
    private BigDecimal totalQuantity;

    public PriceLevel(BigDecimal price) {
        this.price = price;
        this.orders = new LinkedHashMap<>(); // Maintains insertion order for time priority
        this.totalQuantity = BigDecimal.ZERO;
    }

    public void addOrder(Order order) {
        orders.put(order.getOrderId(), order);
        totalQuantity = totalQuantity.add(order.getQuantity());
    }

    public void removeOrder(Order order) {
        if (orders.remove(order.getOrderId()) != null) {
            totalQuantity = totalQuantity.subtract(order.getQuantity());
        }
    }

    public boolean isEmpty() {
        return orders.isEmpty();
    }

    public Map<String, Order> getOrders() {
        return new LinkedHashMap<>(orders); // Return a copy to prevent modification
    }
} 