package com.tradar.core.config;

/**
 * Configuration constants for network communication between system components.
 * Defines multicast groups and ports for various services including order entry,
 * market data, and trade reporting.
 *
 * @author Vrushank Patel
 */
public class NetworkConfig {
    public static final String MULTICAST_GROUP = "239.0.0.1";
    public static final int ORDER_ENTRY_PORT = 5001;
    public static final int MARKET_DATA_PORT = 5002;
    public static final int EXECUTION_REPORT_PORT = 5003;
    public static final int TRADE_REPORT_PORT = 5004;
} 