package it.heron4gf.hpluginmessanger.commons.socket;

import it.heron4gf.hpluginmessanger.commons.HPluginMessageAPI;
import lombok.Getter;

import java.net.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class SimpleSocket {

    private DatagramSocket socket;

    @Getter
    private int port;

    public void close() {
        socket.close();
    }

    public SimpleSocket(int port) throws SocketException {
        this.port = port;
        socket = new DatagramSocket(port);
    }

    public void sendMessage(String message, String server) {
        try {
            sendMessage(message, InetAddress.getLocalHost(), HPluginMessageAPI.getServerSocketsList().get(server));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    public void sendMessage(String message, InetAddress address, int port) {
        CompletableFuture<Void> sendFuture = sendMessageAsync(message, address, port);
    }

    private CompletableFuture<Void> sendMessageAsync(String message, InetAddress address, int port) {
        return CompletableFuture.runAsync(() -> {
            try {
                byte[] data = message.getBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                socket.send(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void unregisterListener(ReceiveListener listener) {
        listeners.remove(listener);
    }

    private Set<ReceiveListener> listeners = new HashSet<>();
    public void registerNewListener(ReceiveListener listener) {
        listeners.add(listener);

        if (listeners.size() == 1) {
            receiveMessageAsync();
        }
    }

    public void receiveMessageAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while (true) {
                    socket.receive(packet);
                    String receivedMessage = new String(packet.getData(), 0, packet.getLength());

                    for (ReceiveListener listener : listeners) {
                        listener.receivedMessage(receivedMessage);
                    }

                    // Clear the buffer for the next packet
                    packet.setLength(buffer.length);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}

