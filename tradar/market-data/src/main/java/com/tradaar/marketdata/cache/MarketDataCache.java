package com.tradar.marketdata.cache;

import com.tradar.marketdata.model.MarketDataEntry;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches market data updates and manages market data state for all symbols.
 * Provides functionality to update market data and notify listeners of changes.
 * Thread-safe implementation using concurrent collections.
 *
 * @author Vrushank Patel
 */
@Slf4j
public class MarketDataCache {
    private final Map<String, MarketDataEntry> cache;
    private final List<MarketDataUpdateListener> listeners;

    public MarketDataCache() {
        this.cache = new ConcurrentHashMap<>();
        this.listeners = new ArrayList<>();
    }

    public void updateMarketData(MarketDataEntry entry) {
        cache.put(entry.getSymbol(), entry);
        notifyListeners(entry);
        log.debug("Updated market data for {}: {}", entry.getSymbol(), entry);
    }

    public MarketDataEntry getMarketData(String symbol) {
        return cache.get(symbol);
    }

    public List<MarketDataEntry> getAllMarketData() {
        return new ArrayList<>(cache.values());
    }

    public void addListener(MarketDataUpdateListener listener) {
        listeners.add(listener);
    }

    public void removeListener(MarketDataUpdateListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(MarketDataEntry entry) {
        for (MarketDataUpdateListener listener : listeners) {
            try {
                listener.onMarketDataUpdate(entry);
            } catch (Exception e) {
                log.error("Error notifying listener: {}", e.getMessage(), e);
            }
        }
    }
} 