package xyz.funtimes909.serverseekerv2.network;

import xyz.funtimes909.serverseekerv2.Main;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Connect {
    public static Socket connect(String address, int port) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(address, port), Main.connection_timeout);
            return socket;
        } catch (IOException e) {
            return null;
        }
    };
}
