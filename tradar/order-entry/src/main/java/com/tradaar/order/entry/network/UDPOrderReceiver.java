package com.tradar.order.entry.network;

import com.tradar.core.model.Order;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

@Slf4j
public class UDPOrderReceiver implements Runnable {
    private static final int PORT = 9876;
    private final DatagramSocket socket;
    private volatile boolean running = true;

    public UDPOrderReceiver() throws Exception {
        socket = new DatagramSocket(PORT);
    }

    @Override
    public void run() {
        byte[] buffer = new byte[4096];

        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);
                Order order = (Order) ois.readObject();

                log.info("Received order: {}", order);
            } catch (Exception e) {
                if (running) {
                    log.error("Error receiving order", e);
                }
            }
        }
    }

    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public static void main(String[] args) {
        try {
            UDPOrderReceiver receiver = new UDPOrderReceiver();
            Thread receiverThread = new Thread(receiver);
            receiverThread.start();
            
            log.info("Order receiver started on port {}", PORT);
            
            // Shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(receiver::stop));
        } catch (Exception e) {
            log.error("Error starting Order Receiver", e);
        }
    }
} 