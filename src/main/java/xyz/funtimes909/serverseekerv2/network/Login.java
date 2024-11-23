package xyz.funtimes909.serverseekerv2.network;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xyz.funtimes909.serverseekerv2.util.VarInt;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Login {
    public static final List<Byte> LOGIN_START = getLoginStart("Herobrine", UUID.randomUUID());

    public static String ping(String ip, short port) {
        try {
            // First ping the server to get the protocol version
            Socket pingSo = Connect.connect(ip, port);
            String pingResponse = Pinger.ping(pingSo);
            JsonObject pingJson = JsonParser.parseString(pingResponse).getAsJsonObject();
            int protocol = pingJson.get("version").getAsJsonObject().get("protocol").getAsInt();

            // Then use that to attempt to connect
            Socket so = Connect.connect(ip, port);
            return ping(so, protocol);
        } catch (Exception e) {
            return null;
        }
    }
    public static String ping(Socket so, int protocol) {
        try {
            OutputStream out = so.getOutputStream();

            // The login request starts off with the Handshake and Login Start
            List<Byte> request = Pinger.getHandshake(protocol, "", (short) 0, (byte) 2);
            request.addAll(LOGIN_START);

            out.write(Bytes.toArray(request));


            InputStream in = so.getInputStream();
            int length = VarInt.decode(in);
            System.out.println("Request length: " + length);
            System.out.println(Arrays.toString(in.readNBytes(length)));

            return "no errors";
        } catch (Exception e) {
            return null;
        }
    }

    public static List<Byte> getLoginStart(String name, UUID id) {
        List<Byte> arr = new ArrayList<>(List.of(
                // Packet ID
                (byte) 0
        ));

        // Username
        if (name.isEmpty())
            arr.add((byte) 0);
        else {
            arr.addAll(VarInt.encode(name.length()));
            arr.addAll(Bytes.asList(name.getBytes(StandardCharsets.UTF_8)));
        }

        // UUID
        arr.addAll(Bytes.asList(Longs.toByteArray(id.getMostSignificantBits())));
        arr.addAll(Bytes.asList(Longs.toByteArray(id.getLeastSignificantBits())));

        // Finally start it with the size
        arr.addAll(0, VarInt.encode(arr.size()));

        return arr;
    }

    public static void main(String[] args) {
        System.out.println(ping("127.0.0.1", (short) 25565));
    }
}
