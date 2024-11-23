package xyz.funtimes909.serverseekerv2.network.protocols;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kotlin.Pair;
import xyz.funtimes909.serverseekerv2.network.Connect;
import xyz.funtimes909.serverseekerv2.network.PacketUtils;
import xyz.funtimes909.serverseekerv2.util.VarInt;
import xyz.funtimes909.serverseekerv2.util.VarTypes;

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

    public static String login(String ip, short port) {
        int protocol = 0;

        // First ping the server to get the protocol version
        try (Socket so = Connect.connect(ip, port)) {
            String status = Handshake.ping(so);
            JsonObject pingJson = JsonParser.parseString(status).getAsJsonObject();
            protocol = pingJson.get("version").getAsJsonObject().get("protocol").getAsInt();
        } catch (Exception ignored) {}

        System.out.println("Protocol version: " + protocol);

        // Then try to login
        try (Socket so = Connect.connect(ip, port)) {
            String login = login(so, protocol);
        } catch (Exception ignored) {}

        return null;
    }
    public static String login(Socket so, int protocol) {
        try (
                OutputStream out = so.getOutputStream();
                InputStream in = so.getInputStream();
                ) {
            // The login request starts off with the Handshake and Login Start
            List<Byte> request = Handshake.getHandshake(protocol, "", (short) 0, (byte) 2);
            request.addAll(LOGIN_START);

            // Write the things to the server
            out.write(Bytes.toArray(request));

            // And get its response
            List<Byte> packet = PacketUtils.readStream(in);
            byte[] packetBa = Bytes.toArray(packet);

            int pointer = 1;

//            System.out.println(packet);
//            System.out.println(VarTypes.readString(packetBa, 1).component1());
//            PacketUtils.readStream(in);
//            System.out.println(VarTypes.readString(packetBa, 18).component1());

            Pair<String, Integer> serverID = VarTypes.readString(packetBa, pointer);
            pointer += serverID.component2();

            Pair<List<Byte>, Integer> publicKey = VarTypes.readByteArray(packetBa, pointer);
            pointer += publicKey.component2();

            Pair<List<Byte>, Integer> verifyToken = VarTypes.readByteArray(packetBa, pointer);
            pointer += verifyToken.component2();

//            System.out.println("Server ID   : " + serverID.component1());
            System.out.println("Public key  : " + publicKey.component1());
            System.out.println("Verify token: " + verifyToken.component1());
            System.out.println("Should AUTH : " + packetBa[pointer]);

            return "no errors";
        } catch (Exception e) {
            System.out.println(e);
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
        System.out.println(login("127.0.0.1", (short) 25565));
    }
}
