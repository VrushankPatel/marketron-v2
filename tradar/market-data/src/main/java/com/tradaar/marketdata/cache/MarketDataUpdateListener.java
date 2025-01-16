package com.tradar.marketdata.cache;

import com.tradar.marketdata.model.MarketDataEntry;

@FunctionalInterface
public interface MarketDataUpdateListener {
    void onMarketDataUpdate(MarketDataEntry entry);
} 