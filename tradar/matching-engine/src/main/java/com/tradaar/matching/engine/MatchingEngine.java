package com.tradar.matching.engine;

import com.tradar.core.model.Order;
import com.tradar.core.model.OrderSide;
import com.tradar.core.model.OrderStatus;
import com.tradar.core.model.OrderType;
import com.tradar.core.model.Trade;
import com.tradar.matching.engine.book.OrderBook;
import com.tradar.matching.engine.book.PriceLevel;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.TreeMap;

/**
 * Core matching engine that processes incoming orders and generates trades.
 * Implements price-time priority matching algorithm for market and limit orders.
 * Maintains separate order books for each trading symbol.
 *
 * @author Vrushank Patel
 */
@Slf4j
public class MatchingEngine {
    private final Map<String, OrderBook> orderBooks;

    public MatchingEngine() {
        this.orderBooks = new ConcurrentHashMap<>();
    }

    public List<Trade> processOrder(Order order) {
        OrderBook orderBook = orderBooks.computeIfAbsent(order.getSymbol(), OrderBook::new);
        List<Trade> trades = new ArrayList<>();

        // For market orders, try to match immediately
        if (order.getType() == OrderType.MARKET) {
            trades.addAll(matchMarketOrder(order, orderBook));
        } else {
            trades.addAll(matchLimitOrder(order, orderBook));
            
            // If order is not fully filled, add to book
            if (order.getStatus() != OrderStatus.FILLED) {
                orderBook.addOrder(order);
            }
        }

        return trades;
    }

    private List<Trade> matchMarketOrder(Order order, OrderBook orderBook) {
        List<Trade> trades = new ArrayList<>();
        TreeMap<BigDecimal, PriceLevel> opposingLevels = 
            order.getSide() == OrderSide.BUY ? orderBook.getSellLevels() : orderBook.getBuyLevels();

        BigDecimal remainingQty = order.getQuantity();

        while (remainingQty.compareTo(BigDecimal.ZERO) > 0 && !opposingLevels.isEmpty()) {
            PriceLevel bestLevel = opposingLevels.firstEntry().getValue();
            trades.addAll(matchOrdersAtLevel(order, bestLevel, remainingQty));
            remainingQty = order.getQuantity().subtract(
                trades.stream()
                    .map(Trade::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
            );
        }

        return trades;
    }

    private List<Trade> matchLimitOrder(Order order, OrderBook orderBook) {
        List<Trade> trades = new ArrayList<>();
        TreeMap<BigDecimal, PriceLevel> opposingLevels = 
            order.getSide() == OrderSide.BUY ? orderBook.getSellLevels() : orderBook.getBuyLevels();

        while (!opposingLevels.isEmpty()) {
            PriceLevel bestLevel = opposingLevels.firstEntry().getValue();
            
            // Check if price is acceptable
            if (order.getSide() == OrderSide.BUY && bestLevel.getPrice().compareTo(order.getPrice()) > 0 ||
                order.getSide() == OrderSide.SELL && bestLevel.getPrice().compareTo(order.getPrice()) < 0) {
                break;
            }

            BigDecimal remainingQty = order.getQuantity().subtract(
                trades.stream()
                    .map(Trade::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
            );

            if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            trades.addAll(matchOrdersAtLevel(order, bestLevel, remainingQty));
        }

        return trades;
    }

    private List<Trade> matchOrdersAtLevel(Order incomingOrder, PriceLevel priceLevel, BigDecimal remainingQty) {
        List<Trade> trades = new ArrayList<>();
        Map<String, Order> ordersAtLevel = priceLevel.getOrders();

        for (Order restingOrder : ordersAtLevel.values()) {
            BigDecimal matchQty = remainingQty.min(restingOrder.getQuantity());
            trades.add(Trade.createTrade(
                incomingOrder.getSide() == OrderSide.BUY ? incomingOrder : restingOrder,
                incomingOrder.getSide() == OrderSide.SELL ? incomingOrder : restingOrder,
                matchQty
            ));

            remainingQty = remainingQty.subtract(matchQty);
            if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
        }

        return trades;
    }
} 