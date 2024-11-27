package xyz.funtimes909.serverseekerv2.network.protocols;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import xyz.funtimes909.serverseekerv2.network.Connect;
import xyz.funtimes909.serverseekerv2.network.PacketUtils;
import xyz.funtimes909.serverseekerv2.types.varlen.VarInt;
import com.google.common.primitives.Bytes;
import xyz.funtimes909.serverseekerv2.types.varlen.VarString;


public class Handshake {
    public static final byte[] REQUEST;
    static {
        List<Byte> request = getHandshake(0, ":3", (short) 0, (byte) 1);
        // Status Request
        request.add((byte) 1); // Size
        request.add((byte) 0); // ID
        REQUEST = Bytes.toArray(request);
    }

    public static List<Byte> getHandshake(int protocol, String ip, short port, byte state) {
        List<Byte> arr = new ArrayList<>(List.of(
                // Packet ID
                (byte) 0
        ));

        // Protocol
        arr.addAll(VarInt.encode(protocol));

        // Address
        if (ip.isEmpty())
            arr.add((byte) 0);
        else {
            arr.addAll(VarInt.encode(ip.length()));
            arr.addAll(Bytes.asList(ip.getBytes(StandardCharsets.UTF_8)));
        }

        // Port (as a short)
        arr.add((byte) (port >> 8));
        arr.add((byte) port);

        // Next State
        // 1: status, 2: login, 3: transfer
        arr.add(state);

        // Finally start it with the size
        arr.addAll(0, VarInt.encode(arr.size()));

        return arr;
    }



    public static String ping(Socket connection) {
        try (
                OutputStream out = connection.getOutputStream();
                InputStream in = connection.getInputStream();
                ) {
            // Write request
            out.write(REQUEST);
            // Read the packet
            byte[] packet = PacketUtils.readStream(in);

            // Start at the 2nd byte as the first is the protocol version (which is always 0)
            return VarString.decode(packet, 1).get();
        } catch (Exception ignored) {
            return null;
        }
    }

    public static void main(String[] args) {
        Socket so = Connect.connect("127.0.0.1", 25565);
        System.out.println(ping(so));
    }
}
