package com.tradaar.order.entry.ui;

import com.tradar.core.model.*;
import com.tradar.order.entry.network.UDPOrderSender;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/**
 * A Swing-based form for submitting trading orders to the system.
 * This class provides a user interface for traders to enter and submit orders
 * with various parameters like symbol, side, type, quantity, and price.
 *
 * @author Vrushank Patel
 */
@Slf4j
public class OrderEntryForm extends JFrame {
    private final UDPOrderSender orderSender;
    private JTextField symbolField;
    private JComboBox<OrderSide> sideCombo;
    private JComboBox<OrderType> typeCombo;
    private JTextField quantityField;
    private JTextField priceField;
    private JComboBox<TimeInForce> tifCombo;
    private final JLabel statusLabel;

    public OrderEntryForm() throws Exception {
        orderSender = new UDPOrderSender("239.0.0.1", 5001);
        setupUI();
        
        orderSender.setOrderStatusListener(status -> {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Last Order Status: " + status);
            });
        });
    }

    private void setupUI() {
        setTitle("Order Entry");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(7, 2, 5, 5));

        // Symbol
        add(new JLabel("Symbol:"));
        symbolField = new JTextField();
        add(symbolField);

        // Side
        add(new JLabel("Side:"));
        sideCombo = new JComboBox<>(OrderSide.values());
        add(sideCombo);

        // Type
        add(new JLabel("Order Type:"));
        typeCombo = new JComboBox<>(OrderType.values());
        add(typeCombo);

        // Quantity
        add(new JLabel("Quantity:"));
        quantityField = new JTextField();
        add(quantityField);

        // Price
        add(new JLabel("Price:"));
        priceField = new JTextField();
        add(priceField);

        // Time in Force
        add(new JLabel("Time in Force:"));
        tifCombo = new JComboBox<>(TimeInForce.values());
        add(tifCombo);

        // Add status label
        statusLabel = new JLabel("Ready to submit orders");
        add(statusLabel);

        // Submit Button
        JButton submitButton = new JButton("Submit Order");
        submitButton.addActionListener(e -> submitOrder());
        add(submitButton);

        pack();
        setLocationRelativeTo(null);
    }

    private void submitOrder() {
        try {
            Order order = Order.createNewOrder(
                symbolField.getText().toUpperCase(),
                (OrderSide) sideCombo.getSelectedItem(),
                (OrderType) typeCombo.getSelectedItem(),
                new BigDecimal(quantityField.getText()),
                new BigDecimal(priceField.getText()),
                (TimeInForce) tifCombo.getSelectedItem()
            );

            statusLabel.setText("Sending order...");
            orderSender.sendOrder(order);
            
            // Don't clear form immediately, wait for confirmation
        } catch (Exception ex) {
            log.error("Error submitting order", ex);
            statusLabel.setText("Error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error submitting order: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new OrderEntryForm().setVisible(true);
            } catch (Exception e) {
                log.error("Error starting Order Entry Form", e);
            }
        });
    }
} 