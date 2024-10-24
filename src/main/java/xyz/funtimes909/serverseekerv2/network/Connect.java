package xyz.funtimes909.serverseekerv2.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Connect {
    private final String address;
    private final int port;
    private final int timeout;

    public Connect(String address, int port, int timeout) {
        this.address = address;
        this.port = port;
        this.timeout = timeout;
    }

    // Attempt to connect to a server and return a Socket object if successful
    public static Socket connect(String address, int port, int timeout) {
        try {
            Socket socket = new Socket();
            // Socket options
            socket.setSoTimeout(timeout);
            socket.connect(new InetSocketAddress(address, port));
            return socket;
        } catch (IOException e) {
            // Fail the future here, socket is invalid
            CompletableFuture.failedFuture(new SocketException());
        }
        return null;
    };
}
