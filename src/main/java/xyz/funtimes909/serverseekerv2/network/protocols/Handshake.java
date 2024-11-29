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
import xyz.funtimes909.serverseekerv2.util.PacketFormatter;


public class Handshake {
    public static final byte[] REQUEST;
    static {
        List<Byte> request = PacketFormatter.encodePacket(0,
                0, // Minecraft Protocol Version
                ":3", // Server Address
                (short) 0, // Port
                (byte) 1 // Next State (1: status, 2: login, 3: transfer)
        );
        // Status Request
        request.add((byte) 1); // Size
        request.add((byte) 0); // ID

        REQUEST = Bytes.toArray(request);
    }


    public static String ping(Socket connection) {
        try (
                OutputStream out = connection.getOutputStream();
                InputStream in = connection.getInputStream();
                ) {
            // Write request
            out.write(REQUEST);

            // Start reading the packet
            int size = VarInt.decode(in);
            return VarString.decode(
                    in.readNBytes(size),
                    // Start at the 2nd byte as the first is the protocol version (which is always 0)
                    1
            ).get();
        } catch (Exception ignored) {
            return null;
        }
    }



    public static void main(String[] args) {
        System.out.println(Bytes.asList(REQUEST));
        Socket so = Connect.connect("127.0.0.1", 25565);
        System.out.println(ping(so));
    }
}
