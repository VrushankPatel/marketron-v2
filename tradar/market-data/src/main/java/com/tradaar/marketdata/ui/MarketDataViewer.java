package com.tradar.marketdata.ui;

import com.tradar.marketdata.cache.MarketDataCache;
import com.tradar.marketdata.fix.MarketDataClient;
import com.tradar.marketdata.model.MarketDataEntry;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * A real-time market data display component that shows current market prices,
 * volumes, and other market statistics. Allows users to subscribe to specific
 * symbols and view their market data updates in real-time.
 *
 * @author Vrushank Patel
 */
@Slf4j
public class MarketDataViewer extends JFrame {
    private final MarketDataCache cache;
    private final MarketDataClient client;
    private final JTable marketDataTable;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private final DefaultTableModel model;

    public MarketDataViewer(MarketDataCache cache, MarketDataClient client) {
        this.cache = cache;
        this.client = client;

        // Initialize market data client
        try {
            client.connect("239.0.0.1", 5002);  // Connect to multicast market data feed
        } catch (Exception e) {
            log.error("Failed to connect to market data feed", e);
        }

        setTitle("Market Data Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 600);

        // Create table
        String[] columns = {
            "Symbol", "Bid", "Bid Size", "Ask", "Ask Size",
            "Last", "Last Size", "Volume", "High", "Low", "Time"
        };
        marketDataTable = new JTable(new DefaultTableModel(columns, 0));

        // Create subscription panel
        JPanel subscriptionPanel = new JPanel();
        JTextField symbolField = new JTextField(10);
        JButton subscribeButton = new JButton("Subscribe");
        
        subscribeButton.addActionListener(e -> {
            String symbol = symbolField.getText().trim().toUpperCase();
            if (!symbol.isEmpty()) {
                client.subscribeMarketData(symbol);
                symbolField.setText("");
            }
        });

        subscriptionPanel.add(new JLabel("Symbol:"));
        subscriptionPanel.add(symbolField);
        subscriptionPanel.add(subscribeButton);

        // Layout
        setLayout(new BorderLayout());
        add(new JScrollPane(marketDataTable), BorderLayout.CENTER);
        add(subscriptionPanel, BorderLayout.NORTH);

        // Register for updates
        cache.addListener(this::updateMarketData);

        // Add auto-refresh timer
        Timer refreshTimer = new Timer(1000, e -> refreshTable());
        refreshTimer.start();
    }

    private void refreshTable() {
        SwingUtilities.invokeLater(() -> {
            // Get all current market data entries
            cache.getAllEntries().forEach(this::updateMarketData);
        });
    }

    private void updateMarketData(MarketDataEntry entry) {
        SwingUtilities.invokeLater(() -> {
            DefaultTableModel model = (DefaultTableModel) marketDataTable.getModel();
            boolean found = false;

            // Update existing row or add new one
            for (int i = 0; i < model.getRowCount(); i++) {
                if (model.getValueAt(i, 0).equals(entry.getSymbol())) {
                    updateRow(model, i, entry);
                    found = true;
                    break;
                }
            }

            if (!found) {
                addRow(model, entry);
            }
        });
    }

    private void updateRow(DefaultTableModel model, int row, MarketDataEntry entry) {
        model.setValueAt(entry.getBidPrice(), row, 1);
        model.setValueAt(entry.getBidSize(), row, 2);
        model.setValueAt(entry.getAskPrice(), row, 3);
        model.setValueAt(entry.getAskSize(), row, 4);
        model.setValueAt(entry.getLastPrice(), row, 5);
        model.setValueAt(entry.getLastSize(), row, 6);
        model.setValueAt(entry.getVolume(), row, 7);
        model.setValueAt(entry.getHigh(), row, 8);
        model.setValueAt(entry.getLow(), row, 9);
        model.setValueAt(entry.getTimestamp().format(formatter), row, 10);
    }

    private void addRow(DefaultTableModel model, MarketDataEntry entry) {
        model.addRow(new Object[]{
            entry.getSymbol(),
            entry.getBidPrice(),
            entry.getBidSize(),
            entry.getAskPrice(),
            entry.getAskSize(),
            entry.getLastPrice(),
            entry.getLastSize(),
            entry.getVolume(),
            entry.getHigh(),
            entry.getLow(),
            entry.getTimestamp().format(formatter)
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MarketDataCache cache = new MarketDataCache();
            MarketDataClient client = new MarketDataClient(cache);
            MarketDataViewer viewer = new MarketDataViewer(cache, client);
            viewer.setVisible(true);
        });
    }
} 