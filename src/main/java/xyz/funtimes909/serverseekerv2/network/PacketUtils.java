package xyz.funtimes909.serverseekerv2.network;

import com.google.common.primitives.Bytes;
import kotlin.Pair;
import xyz.funtimes909.serverseekerv2.util.VarInt;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PacketUtils {
    public static byte[] readStream(InputStream io) throws IOException {
        int size = VarInt.decode(io);
        return io.readNBytes(size);
    }
}