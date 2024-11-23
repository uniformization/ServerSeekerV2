package xyz.funtimes909.serverseekerv2.network;

import com.google.common.primitives.Bytes;
import xyz.funtimes909.serverseekerv2.util.VarInt;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Pinger {
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
            // Skip the first varint which indicates the total size of the packet.
            // Later we properly read a varint that contains the length of the json, so we use that
            for (byte i = 0; i < 5; i ++)
                if ((((byte) in.read()) & 0b10000000) == 0)
                    break;
            // Read the packet id, which should always be 0
            in.read();
            // Properly read the varint. This one contains the length of the following string
            int json_length = VarInt.decode(in);
            // Finally read the bytes
            byte[] status = in.readNBytes(json_length);
            // Close all resources
            connection.close();
            in.close();
            return new String(status);
        } catch (Exception e) {
            return null;
        }
    }
}
