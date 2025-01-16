package com.tradar.order.entry.network;

import com.tradar.core.model.Order;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

@Slf4j
public class UDPOrderSender {
    private static final int PORT = 9876;
    private final DatagramSocket socket;
    private final InetAddress address;

    public UDPOrderSender() throws Exception {
        socket = new DatagramSocket();
        address = InetAddress.getLocalHost();
    }

    public void sendOrder(Order order) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(order);
            byte[] data = baos.toByteArray();

            DatagramPacket packet = new DatagramPacket(data, data.length, address, PORT);
            socket.send(packet);
            log.info("Order sent: {}", order);
        } catch (Exception e) {
            log.error("Error sending order", e);
        }
    }

    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
} 