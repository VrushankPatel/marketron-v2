package com.tradaar.reporting.ui;

import com.tradar.core.model.ExecutionReport;
import com.tradar.core.model.TradeCaptureReport;
import com.tradar.reporting.repository.ReportRepository;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * A comprehensive trade reporting interface that displays execution reports
 * and trade capture reports. Provides filtering and viewing capabilities
 * for historical trade data and real-time updates.
 *
 * @author Vrushank Patel
 */
@Slf4j
public class ReportViewer extends JFrame {
    private final ReportRepository repository;
    private final JTabbedPane tabbedPane;
    private final JTable executionTable;
    private final JTable tradeTable;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReportViewer(ReportRepository repository) {
        this.repository = repository;
        
        // Start listening for new reports
        try {
            repository.startListening("239.0.0.1", 5003);  // Listen for execution reports
        } catch (Exception e) {
            log.error("Failed to start listening for reports", e);
        }

        // Add report listener
        repository.addReportListener(report -> {
            if (report instanceof ExecutionReport) {
                SwingUtilities.invokeLater(() -> 
                    updateExecutionTable(List.of((ExecutionReport) report)));
            } else if (report instanceof TradeCaptureReport) {
                SwingUtilities.invokeLater(() -> 
                    updateTradeTable(List.of((TradeCaptureReport) report)));
            }
        });

        setTitle("Trade Reports");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 600);
        
        tabbedPane = new JTabbedPane();
        
        // Execution Reports Panel
        executionTable = createExecutionReportTable();
        JPanel execPanel = new JPanel(new BorderLayout());
        execPanel.add(new JScrollPane(executionTable), BorderLayout.CENTER);
        execPanel.add(createExecutionFilterPanel(), BorderLayout.NORTH);
        
        // Trade Reports Panel
        tradeTable = createTradeReportTable();
        JPanel tradePanel = new JPanel(new BorderLayout());
        tradePanel.add(new JScrollPane(tradeTable), BorderLayout.CENTER);
        tradePanel.add(createTradeFilterPanel(), BorderLayout.NORTH);
        
        tabbedPane.addTab("Execution Reports", execPanel);
        tabbedPane.addTab("Trade Reports", tradePanel);
        
        add(tabbedPane);
        
        // Refresh timer
        Timer timer = new Timer(5000, e -> refreshTables());
        timer.start();
    }

    private JTable createExecutionReportTable() {
        String[] columns = {
            "Exec ID", "Order ID", "Symbol", "Side", "Type",
            "Order Qty", "Leaves Qty", "Cum Qty", "Avg Price",
            "Status", "Time", "Text"
        };
        return new JTable(new DefaultTableModel(columns, 0));
    }

    private JTable createTradeReportTable() {
        String[] columns = {
            "Trade Report ID", "Trade ID", "Symbol",
            "Buy Order ID", "Sell Order ID",
            "Price", "Quantity", "Trade Date", "Time"
        };
        return new JTable(new DefaultTableModel(columns, 0));
    }

    private JPanel createExecutionFilterPanel() {
        JPanel panel = new JPanel();
        JTextField orderIdField = new JTextField(10);
        JButton filterButton = new JButton("Filter by Order ID");
        
        filterButton.addActionListener(e -> {
            String orderId = orderIdField.getText().trim();
            if (!orderId.isEmpty()) {
                updateExecutionTable(repository.getExecutionReportsByOrderId(orderId));
            }
        });
        
        panel.add(new JLabel("Order ID:"));
        panel.add(orderIdField);
        panel.add(filterButton);
        return panel;
    }

    private JPanel createTradeFilterPanel() {
        JPanel panel = new JPanel();
        JTextField symbolField = new JTextField(10);
        JButton filterButton = new JButton("Filter by Symbol");
        
        filterButton.addActionListener(e -> {
            String symbol = symbolField.getText().trim();
            if (!symbol.isEmpty()) {
                updateTradeTable(repository.getTradeReportsBySymbol(symbol));
            }
        });
        
        panel.add(new JLabel("Symbol:"));
        panel.add(symbolField);
        panel.add(filterButton);
        return panel;
    }

    private void refreshTables() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusHours(24); // Last 24 hours
        
        updateExecutionTable(repository.getExecutionReports(from, now));
        updateTradeTable(repository.getTradeReports(from, now));
    }

    private void updateExecutionTable(List<ExecutionReport> reports) {
        DefaultTableModel model = (DefaultTableModel) executionTable.getModel();
        model.setRowCount(0);
        
        for (ExecutionReport report : reports) {
            model.addRow(new Object[]{
                report.getExecId(),
                report.getOrderId(),
                report.getSymbol(),
                report.getSide(),
                report.getOrderType(),
                report.getOrderQty(),
                report.getLeavesQty(),
                report.getCumQty(),
                report.getAvgPx(),
                report.getOrderStatus(),
                report.getTransactTime().format(formatter),
                report.getText()
            });
        }
    }

    private void updateTradeTable(List<TradeCaptureReport> reports) {
        DefaultTableModel model = (DefaultTableModel) tradeTable.getModel();
        model.setRowCount(0);
        
        for (TradeCaptureReport report : reports) {
            model.addRow(new Object[]{
                report.getTradeReportId(),
                report.getTradeId(),
                report.getSymbol(),
                report.getBuyOrderId(),
                report.getSellOrderId(),
                report.getPrice(),
                report.getQuantity(),
                report.getTradeDate().format(formatter),
                report.getTransactTime().format(formatter)
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ReportViewer viewer = new ReportViewer(new ReportRepository());
            viewer.setVisible(true);
        });
    }
} 