package com.tradar.matching.engine.book;

import com.tradar.core.model.Order;
import com.tradar.core.model.OrderSide;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;

/**
 * Implements a limit order book for a single trading symbol.
 * Maintains separate trees for buy and sell orders, sorted by price-time priority.
 * Provides methods to add and remove orders and query best prices.
 *
 * @author Vrushank Patel
 */
@Slf4j
public class OrderBook {
    private final String symbol;
    @Getter
    private final TreeMap<BigDecimal, PriceLevel> buyLevels;
    @Getter
    private final TreeMap<BigDecimal, PriceLevel> sellLevels;
    
    public OrderBook(String symbol) {
        this.symbol = symbol;
        this.buyLevels = new TreeMap<>(Collections.reverseOrder()); // Highest price first for buys
        this.sellLevels = new TreeMap<>(); // Lowest price first for sells
    }

    public void addOrder(Order order) {
        TreeMap<BigDecimal, PriceLevel> levels = order.getSide() == OrderSide.BUY ? buyLevels : sellLevels;
        BigDecimal price = order.getPrice();
        
        PriceLevel level = levels.computeIfAbsent(price, k -> new PriceLevel(price));
        level.addOrder(order);
        
        log.info("Added {} order to book: {}", order.getSide(), order);
    }

    public void removeOrder(Order order) {
        TreeMap<BigDecimal, PriceLevel> levels = order.getSide() == OrderSide.BUY ? buyLevels : sellLevels;
        PriceLevel level = levels.get(order.getPrice());
        
        if (level != null) {
            level.removeOrder(order);
            if (level.isEmpty()) {
                levels.remove(order.getPrice());
            }
            log.info("Removed {} order from book: {}", order.getSide(), order);
        }
    }

    public BigDecimal getBestBid() {
        return buyLevels.isEmpty() ? null : buyLevels.firstKey();
    }

    public BigDecimal getBestAsk() {
        return sellLevels.isEmpty() ? null : sellLevels.firstKey();
    }

    @Override
    public String toString() {
        return String.format("OrderBook{symbol='%s', bestBid=%s, bestAsk=%s}", 
            symbol, getBestBid(), getBestAsk());
    }
} 