package xyz.funtimes909.serverseekerv2.network.protocols;

import com.google.common.primitives.Bytes;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xyz.funtimes909.serverseekerv2.network.Connect;
import xyz.funtimes909.serverseekerv2.network.PacketUtils;
import xyz.funtimes909.serverseekerv2.types.LoginAttempt;
import xyz.funtimes909.serverseekerv2.types.protocols.login.incoming.Compression;
import xyz.funtimes909.serverseekerv2.util.PacketFormatter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class QuickLogin {
    /** A really rudimentary login method that can check some basic stats about the server */
    public static LoginAttempt quickLogin(Socket so, int protocol) {
        try (
                OutputStream out = so.getOutputStream();
                InputStream in = so.getInputStream();
        ) {
            // The login request starts off with the Handshake and Login Start
            List<Byte> request = PacketFormatter.encodePacket(0, // Handshake Protocol
                    protocol, // Minecraft Protocol Version
                    "", // Server Address
                    (short) 0, // Port
                    (byte) 2 // Next State (1: status, 2: login, 3: transfer)
            );
            request.addAll(Login.REQUEST);
            // Write the things to the server
            out.write(Bytes.toArray(request));


            int compressionThreshold = -1;
            byte[] packet;

            // Try to get a good response within 5 packets
            loop: for (int i = 0; i < 5; i++) {
                // And get its response
                packet = PacketUtils.readStream(in, compressionThreshold);

                switch (packet[0]) {
                    // Disconnect
                    case 0: return LoginAttempt.ONLY_WHITELIST;
                    // Encryption
                    case 1: return
                            // The final byte (bool) represents weather it's in online or offline mode (client auth?)
                            packet[packet.length - 1] == 1?
                            LoginAttempt.ONLINE:
                            LoginAttempt.OFFLINE;
                    // Success
                    case 2: return LoginAttempt.INSECURE;
                    // Compression
                    case 3: compressionThreshold = Compression.decode(packet).threshold;
                }
            }
        } catch (Exception ignored) { }
        return LoginAttempt.UNKNOWN;
    }



    public static void main(String[] args) {
        String ip = "127.0.0.1";
        int port = 25565;
        int protocol = 0;

        // First ping the server to get the protocol version
        try (Socket so = Connect.connect(ip, port)) {
            String status = Handshake.ping(so);
            JsonObject pingJson = JsonParser.parseString(status).getAsJsonObject();
            protocol = pingJson.get("version").getAsJsonObject().get("protocol").getAsInt();
        } catch (Exception ignored) {}
        // Then try to login
        try (Socket so = Connect.connect(ip, port)) {
            System.out.println(quickLogin(so, protocol));
        } catch (Exception ignored) {}
    }
}
