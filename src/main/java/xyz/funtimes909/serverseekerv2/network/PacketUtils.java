package xyz.funtimes909.serverseekerv2.network;

import com.google.common.primitives.Bytes;
import kotlin.Pair;
import xyz.funtimes909.serverseekerv2.util.VarInt;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PacketUtils {
    public static List<Byte> readStream(InputStream io) throws IOException {
        ArrayList<Byte> result = new ArrayList<>();
        byte[] buff = new byte[1024];

        // A rolling count of the amount of bytes read
        int totalRead = io.read(buff);
        result.addAll(Bytes.asList(buff));
        // All packets are prefixed with a varint of their size, so use that
        Pair<Integer, Byte> packetSize = VarInt.decode(buff);
        int bytesNeeded = packetSize.component1() + packetSize.component2();

        // Read the rest of the bytes
        while (totalRead < bytesNeeded) {
            totalRead += io.read(buff);
            result.addAll(Bytes.asList(buff));
        }

        return result.subList(packetSize.component2(), bytesNeeded);
    }
}