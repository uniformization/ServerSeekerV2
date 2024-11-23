package xyz.funtimes909.serverseekerv2.network.protocols;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import xyz.funtimes909.serverseekerv2.network.Connect;
import xyz.funtimes909.serverseekerv2.network.PacketUtils;
import xyz.funtimes909.serverseekerv2.util.VarInt;
import com.google.common.primitives.Bytes;


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
        try (OutputStream out = connection.getOutputStream()) {
            out.write(REQUEST);

            InputStream in = connection.getInputStream();
            List<Byte> packet = PacketUtils.readStream(in);

            // Set to 1 to skip the protocol version
            byte i = 1;
            // Skip the first varint which indicates the size of the string
            //  we can assume that the rest of the packet is the string
            for (; i < 6; i ++)
                if ((packet.get(i) & 0b10000000) == 0)
                    break;

            // Close all resources
            connection.close();
            in.close();

            return new String(Bytes.toArray(packet.subList(i + 1, packet.size())));
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) {
        Socket so = Connect.connect("127.0.0.1", 25565);
        System.out.println(ping(so));
    }
}
